package net.anatomyworld.harambefmod.entity.boat.musavacca.clientmodel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.entity.boat.musavacca.MusavaccaBoat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

/** State-based renderer (1.21+) for the Musavacca boat. */
public class MusavaccaBoatRenderer extends EntityRenderer<MusavaccaBoat, BoatRenderState> {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "textures/entity/boat/musavacca.png");

    /** 👉 Waterline offset. Smaller = lower hull. */
    public static final double HULL_LIFT = 0.28D;

    /** 👉 Fine centering offsets (X/Z) if you ever need to nudge the model. */
    public static final float MODEL_X_OFFSET = 0.0F;
    public static final float MODEL_Z_OFFSET = 0.0F;

    private final MusavaccaBoatModel model;

    public MusavaccaBoatRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new MusavaccaBoatModel(ctx.bakeLayer(MusavaccaBoatModel.LAYER));
        this.shadowRadius = 0.8F;
    }

    @Override public @NotNull BoatRenderState createRenderState() { return new BoatRenderState(); }

    @Override
    public void extractRenderState(@NotNull MusavaccaBoat boat,
                                   @NotNull BoatRenderState state,
                                   float partialTick) {
        // Fill vanilla fields (hurt wobble, bubble tilt, etc.).
        super.extractRenderState(boat, state, partialTick);

        // Ensure yaw tracks actual sailing heading smoothly.
        state.yRot = Mth.rotLerp(partialTick, boat.yRotO, boat.getYRot());

        // Drive paddles only when actually moving; idle = times=0.
        float speed = (float) boat.getDeltaMovement().horizontalDistance();
        if (speed > 0.02F) {
            float t = boat.tickCount + partialTick;
            state.rowingTimeLeft  = t * 0.06F;
            state.rowingTimeRight = t * 0.06F + 1.2F; // phase offset for alternating stroke
        } else {
            state.rowingTimeLeft  = 0.0F;
            state.rowingTimeRight = 0.0F;
        }
    }

    @Override
    public void render(@NotNull BoatRenderState state,
                       @NotNull PoseStack ps,
                       @NotNull MultiBufferSource buf,
                       int packedLight) {
        ps.pushPose();

        // Waterline & forward facing
        ps.translate(0.0, HULL_LIFT, 0.0);
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));

        // Damage wobble (X)
        if (state.hurtTime > 0.0F) {
            float wob = Mth.sin(state.hurtTime) * state.hurtTime * state.damageTime / 10.0F * state.hurtDir;
            ps.mulPose(Axis.XP.rotationDegrees(wob));
        }

        // Bubble tilt (Z)
        if (state.bubbleAngle != 0.0F) {
            ps.mulPose(Axis.ZP.rotation(state.bubbleAngle));
        }

        // Blockbench → MC handedness + root pivot at y=24px (1.5 blocks)
        ps.scale(-1.0F, -1.0F, 1.0F);
        ps.translate(MODEL_X_OFFSET, -1.5F, MODEL_Z_OFFSET);

        var vc = buf.getBuffer(RenderType.entityCutoutNoCull(TEX));
        model.setupAnim(state);
        model.renderToBuffer(ps, vc, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        ps.popPose();
        super.render(state, ps, buf, packedLight);
    }

    // Keep both; depending on mappings, one is the actual override.
    public @NotNull ResourceLocation getTextureLocation(@NotNull BoatRenderState state) { return TEX; }
    public @NotNull ResourceLocation getTextureLocation(@NotNull MusavaccaBoat boat)    { return TEX; }
}
