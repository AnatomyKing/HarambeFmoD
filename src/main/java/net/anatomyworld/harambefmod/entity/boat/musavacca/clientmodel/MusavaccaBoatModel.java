// src/main/java/net/anatomyworld/harambefmod/entity/boat/musavacca/clientmodel/MusavaccaBoatModel.java
package net.anatomyworld.harambefmod.entity.boat.musavacca.clientmodel;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

/** Boat model (1.21.x RenderState) — no-startup-spike paddles + phase continuity + natural idle dip. */
public class MusavaccaBoatModel extends EntityModel<BoatRenderState> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "musavacca_boat"), "main");

    /** LOWER this to move the boat UP; raise it to move the boat DOWN. (Pixels; 16 px = 1 block) */
    public static float ROOT_Y = 6.0F;

    /** Rotate model so it faces Minecraft forward (north = -Z). -HALF_PI maps +X → -Z. */
    public static float MODEL_YAW_FIX = -Mth.HALF_PI;

    /** Push paddles outward from the hull (pixels). */
    public static float PADDLE_OUT_EXTRA  = 3.0F;
    /** Drop paddles downward (pixels). */
    public static float PADDLE_DROP_EXTRA = 3.5F;

    /** === Blockbench rest rotation (radians) copied from your export === */
    private static final float BB_BASE_X = 2.1368F;  // pitch
    private static final float BB_BASE_Y = 0.8362F;  // yaw  (left = +, right = -)
    private static final float BB_BASE_Z = 2.8434F;  // roll (left = +, right = -)

    /** Extra "rest in water" tweak. */
    public static float REST_WATER_DIP = 0.20F; // ~11.5°

    /** Stroke amplitudes (additive around the rest pose). */
    public static float YAW_AMP   = 0.40F;
    public static float PITCH_AMP = 0.55F;

    /** If sweep looks inward, flip to -1. */
    public static float YAW_SIDE_SIGN = 1.0F;

    /** Blend responsiveness (amplitude only). Keep modest for zero “kick”. */
    public static float RESP_ACTIVE = 0.16F;
    public static float RESP_IDLE   = 0.10F;

    private final ModelPart root;
    private final ModelPart paddleRight;
    private final ModelPart paddleLeft;

    // Continuous unwrapped phases we render from (so we can resume smoothly after idle)
    private float phaseL = 0f, phaseR = 0f;
    // Last raw angles seen from the render state (as-delivered, radians, wrapped)
    private float prevRawL = Float.NaN, prevRawR = Float.NaN;
    // Last active flags (to detect start/stop)
    private boolean wasActiveL = false, wasActiveR = false;
    // 0..1 amplitude blends (0 = idle at rest pose, 1 = full stroke)
    private float blendL = 0f, blendR = 0f;

    public MusavaccaBoatModel(ModelPart bakedRoot) {
        super(bakedRoot);
        this.root = bakedRoot.getChild("root");
        this.paddleRight = this.root.getChild("paddle_right");
        this.paddleLeft  = this.root.getChild("paddle_left");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        PartDefinition root = part.addOrReplaceChild(
                "root",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, ROOT_Y, 0.0F, 0.0F, MODEL_YAW_FIX, 0.0F)
        );

        root.addOrReplaceChild("bottom",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-16.0F, -14.0F, -22.0F, 16.0F, 14.0F, 44.0F),
                PartPose.offset(8.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("right",
                CubeListBuilder.create().texOffs(21, 73).mirror()
                        .addBox(-3.5F, -3.5F, -22.0F, 7.0F, 7.0F, 44.0F).mirror(false),
                PartPose.offset(-11.5F, -3.5F, 0.0F));

        root.addOrReplaceChild("left",
                CubeListBuilder.create().texOffs(21, 73)
                        .addBox(-3.5F, -3.5F, -22.0F, 7.0F, 7.0F, 44.0F),
                PartPose.offset(11.5F, -3.5F, 0.0F));

        PartDefinition front = root.addOrReplaceChild("front", CubeListBuilder.create(),
                PartPose.offset(0.0F, -17.0352F, -34.0538F));
        front.addOrReplaceChild("front_r1",
                CubeListBuilder.create().texOffs(0, 60)
                        .addBox(-4.0F, -9.5F, -6.5F, 12.0F, 17.0F, 13.0F),
                PartPose.offsetAndRotation(-2.0F, 8.2852F, 7.5538F, 1.1781F, 0.0F, 0.0F));
        front.addOrReplaceChild("front_r2",
                CubeListBuilder.create().texOffs(82, 99)
                        .addBox(-3.0F, -3.0F, -0.5F, 10.0F, 7.0F, 7.0F),
                PartPose.offsetAndRotation(-2.0F, -6.9565F, -4.1989F, 0.7854F, 0.0F, 0.0F));
        front.addOrReplaceChild("front_r3",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 11.0F),
                PartPose.offsetAndRotation(0.0F, 1.1752F, -2.4311F, 0.7854F, 0.0F, 0.0F));

        PartDefinition back = root.addOrReplaceChild("back", CubeListBuilder.create(),
                PartPose.offset(0.0F, -9.1327F, 27.4239F));
        back.addOrReplaceChild("back_r1",
                CubeListBuilder.create().texOffs(0, 60)
                        .addBox(-4.0F, -9.5F, -6.5F, 12.0F, 17.0F, 13.0F),
                PartPose.offsetAndRotation(-2.0F, 0.3827F, -0.9239F, -1.1781F, 0.0F, 0.0F));

        // Paddles (rest rotations from Blockbench)
        root.addOrReplaceChild("paddle_right",
                CubeListBuilder.create().texOffs(82, 73).mirror()
                        .addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).mirror(false)
                        .texOffs(82, 73).mirror()
                        .addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F).mirror(false),
                PartPose.offsetAndRotation(
                        -12.25F - PADDLE_OUT_EXTRA, -13.0F + PADDLE_DROP_EXTRA, -3.0F,
                        BB_BASE_X, -BB_BASE_Y, -BB_BASE_Z));

        root.addOrReplaceChild("paddle_left",
                CubeListBuilder.create().texOffs(82, 73)
                        .addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F)
                        .texOffs(82, 73)
                        .addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F),
                PartPose.offsetAndRotation(
                        12.25F + PADDLE_OUT_EXTRA, -13.0F + PADDLE_DROP_EXTRA, -3.0F,
                        BB_BASE_X,  BB_BASE_Y,  BB_BASE_Z));

        // Optional seat markers
        part.addOrReplaceChild("seat_driver",      CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, -17.0F));
        part.addOrReplaceChild("seat_passenger_1", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F,   0.0F));
        part.addOrReplaceChild("seat_passenger_2", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F,  17.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(@NotNull BoatRenderState s) {
        // 1) Raw values from state are already radians (0 when idle).
        final float lRaw = s.rowingTimeLeft;
        final float rRaw = s.rowingTimeRight;

        final boolean lActive = Math.abs(lRaw) > 1e-6f;
        final boolean rActive = Math.abs(rRaw) > 1e-6f;

        // 2) Unwrap angle across 2π to keep phase continuous between frames.
        if (lActive) {
            if (!wasActiveL || Float.isNaN(prevRawL)) {
                // align to the nearest wrap of the new raw angle (no phase pop on resume)
                phaseL = nearestWrapped(phaseL, lRaw);
            } else {
                phaseL += wrap(lRaw - prevRawL);
            }
            prevRawL = lRaw;
        }
        if (rActive) {
            if (!wasActiveR || Float.isNaN(prevRawR)) {
                phaseR = nearestWrapped(phaseR, rRaw);
            } else {
                phaseR += wrap(rRaw - prevRawR);
            }
            prevRawR = rRaw;
        }
        wasActiveL = lActive;
        wasActiveR = rActive;

        // 3) Pure amplitude blend with gentle easing (no “start kick”).
        blendL = approach(blendL, lActive ? 1f : 0f, lActive ? RESP_ACTIVE : RESP_IDLE);
        blendR = approach(blendR, rActive ? 1f : 0f, rActive ? RESP_ACTIVE : RESP_IDLE);

        final float tL = easeInOutCubic(blendL);
        final float tR = easeInOutCubic(blendR);

        // 4) Apply to parts
        applyPaddle(paddleLeft,  true,  phaseL, tL);
        applyPaddle(paddleRight, false, phaseR, tR);
    }

    private static void applyPaddle(ModelPart p, boolean isLeft, float phase, float amp01) {
        final float side = isLeft ? 1f : -1f;

        // Base = Blockbench rest, dipped slightly into water (natural idle)
        float baseY =  side * BB_BASE_Y;           // yaw
        float baseZ =  side * BB_BASE_Z;           // roll (blade twist)
        float baseX =  BB_BASE_X + REST_WATER_DIP; // pitch (dip)

        // Stroke deltas (around rest)
        float yawDelta   = YAW_AMP   * Mth.sin(phase) * side * YAW_SIDE_SIGN;
        float pitchDelta = PITCH_AMP * Mth.sin(phase + 1.2F);

        // Target stroke pose
        float targetY = baseY + yawDelta;
        float targetX = baseX + pitchDelta;
        float targetZ = baseZ; // keep twist constant

        // Blend from rest → stroke based on amplitude only
        p.yRot = Mth.lerp(amp01, baseY, targetY);
        p.xRot = Mth.lerp(amp01, baseX, targetX);
        p.zRot = Mth.lerp(amp01, baseZ, targetZ);
    }

    /** Wrap a delta to [-pi, pi] (for stable unwrapping). */
    private static float wrap(float a) {
        float twoPi = (float)(Math.PI * 2.0);
        a = (a + (float)Math.PI) % twoPi;
        if (a < 0f) a += twoPi;
        return a - (float)Math.PI;
    }

    /** Choose value + k*2pi nearest to reference. */
    private static float nearestWrapped(float reference, float value) {
        float twoPi = (float)(Math.PI * 2.0);
        float k = Math.round((reference - value) / twoPi);
        return value + k * twoPi;
    }

    /** Critically-damped step toward target (frame-rate agnostic feel). */
    private static float approach(float current, float target, float factor) {
        return current + (target - current) * Mth.clamp(factor, 0f, 1f);
    }

    /** Smooth easing for amplitude (no kick). */
    private static float easeInOutCubic(float x) {
        x = Mth.clamp(x, 0f, 1f);
        return x < 0.5f ? 4f * x * x * x : 1f - (float)Math.pow(-2f * x + 2f, 3) / 2f;
    }
}
