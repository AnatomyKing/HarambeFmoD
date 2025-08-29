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

/** Blockbench boat adapted to 1.21.x RenderState pipeline. */
public class MusavaccaBoatModel extends EntityModel<BoatRenderState> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "musavacca_boat"), "main");

    private final ModelPart root;
    private final ModelPart paddleRight;
    private final ModelPart paddleLeft;

    public MusavaccaBoatModel(ModelPart bakedRoot) {
        super(bakedRoot);
        this.root = bakedRoot.getChild("root");
        this.paddleRight = this.root.getChild("paddle_right");
        this.paddleLeft  = this.root.getChild("paddle_left");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

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

        root.addOrReplaceChild("paddle_right",
                CubeListBuilder.create().texOffs(82, 73).mirror()
                        .addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).mirror(false)
                        .texOffs(82, 73).mirror()
                        .addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F).mirror(false),
                PartPose.offsetAndRotation(-13.25F, -10.0F, -3.0F, 2.1368F, -0.8362F, -2.8434F));

        root.addOrReplaceChild("paddle_left",
                CubeListBuilder.create().texOffs(82, 73)
                        .addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F)
                        .texOffs(82, 73)
                        .addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F),
                PartPose.offsetAndRotation(13.25F, -10.0F, -3.0F, 2.1642F, 0.8727F, 2.8798F));

        // Optional seat markers (empty parts)
        part.addOrReplaceChild("seat_driver", CubeListBuilder.create(), PartPose.offset(0.0F, 10.0F, -12.0F));
        part.addOrReplaceChild("seat_passenger_1", CubeListBuilder.create(), PartPose.offset(0.0F, 10.0F, -1.0F));
        part.addOrReplaceChild("seat_passenger_2", CubeListBuilder.create(), PartPose.offset(0.0F, 10.0F, 12.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(@NotNull BoatRenderState s) {
        // If row times are zero, treat as "idle" so paddles don't wave around.
        if (s.rowingTimeLeft == 0.0F && s.rowingTimeRight == 0.0F) {
            setPaddleRest(true,  this.paddleLeft);
            setPaddleRest(false, this.paddleRight);
            return;
        }

        // Vanilla-style stroke: yaw outward/inward + pitch dip. Mirror by side.
        setPaddleAngles(s.rowingTimeLeft,  true,  this.paddleLeft);
        setPaddleAngles(s.rowingTimeRight, false, this.paddleRight);
    }

    private static void setPaddleRest(boolean isLeft, ModelPart paddle) {
        paddle.yRot = isLeft ? 0.45F : -0.45F;   // mild flare
        paddle.xRot = -0.6F;                     // shallow dip
        paddle.zRot = 0.0F;
    }

    /** Close to vanilla’s paddle math. */
    private static void setPaddleAngles(float time, boolean isLeft, ModelPart paddle) {
        float phase = time * (float)Math.PI * 2F;
        float sweep = Mth.sin(phase);                  // [-1..1], sideways sweep
        float drive = Mth.sin(phase + 1.2F);           // offset for “catch”

        // Yaw (sideways): paddle flares outward then back in. Mirror for sides.
        float yawBase = isLeft ? 0.55F : -0.55F;       // ~±31.5°
        float yawAmp  = 0.45F;                         // add ~±25°
        paddle.yRot   = yawBase + yawAmp * sweep * (isLeft ? 1F : -1F);

        // Pitch (blade dips into water then recovers) ~[-78°, -5°]
        float pitchMin = -1.36F;
        float pitchMax = -0.09F;
        float t = (drive * 0.5F + 0.5F);               // 0..1
        paddle.xRot = Mth.lerp(t, pitchMin, pitchMax);

        paddle.zRot = 0.0F; // no roll
    }
}
