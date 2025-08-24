package net.anatomyworld.harambefmod.entity.bananacow.clientmodel;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class BananaCowModel extends EntityModel<BananaCowModel.State> {

    /** Minimal custom state (embedded so there’s no extra file). */
    public static class State extends LivingEntityRenderState {
        public float headYawDeg;
        public float headPitchDeg;
        public float limbSwing;
        public float limbSwingAmount;
        public float ageTicks;
    }

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "banana_cow"),
                    "main"
            );

    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart rightFrontLeg, leftFrontLeg, rightBackLeg, leftBackLeg;

    public BananaCowModel(ModelPart bakedRoot) {
        super(bakedRoot); // 1.21.x: pass root to super
        ModelPart root = bakedRoot.getChild("root");
        this.head          = root.getChild("head");
        this.tail          = root.getChild("body").getChild("tail");
        this.leftBackLeg   = root.getChild("left_back_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg  = root.getChild("left_front_leg");
        this.rightBackLeg  = root.getChild("right_back_leg");
    }

    /* ===== Geometry (unchanged from your Blockbench export) ===== */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        PartDefinition root = part.addOrReplaceChild("root",
                CubeListBuilder.create(),
                PartPose.offset(0, 24, -1));

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create(),
                PartPose.offset(0, -12.5F, -7));

        head.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(35, 29)
                        .addBox(-4, -4.1F, -8.5F, 8, 8, 9),
                PartPose.offsetAndRotation(0, -0.5796F, 0.2059F, rad(-22.5F), 0, 0));

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-1, -19, 2, rad(90), 0, 0));

        body.addOrReplaceChild("torso",
                CubeListBuilder.create()
                        .texOffs(0, 0) .addBox(-5, -9, -5, 10, 18, 10)
                        .texOffs(0,54).addBox(-2,  0, -6,  4,  4,  1),
                PartPose.offset(1, -1, -6));

        PartDefinition tail = body.addOrReplaceChild("tail",
                CubeListBuilder.create(),
                PartPose.offset(1, 7.25F, -5.75F));

        tail.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(0, 29)
                        .addBox(-4, -2.5F, -4.5F, 8, 15, 9),
                PartPose.offsetAndRotation(0, 1.0035F, -0.2314F, rad(22.5F), 0, 0));

        PartDefinition tip = tail.addOrReplaceChild("tip",
                CubeListBuilder.create(),
                PartPose.offset(-2, 14.2535F, 0.5186F));

        tip.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                        .texOffs(41,19).addBox(-3,  4.6543F, 3.2242F, 6, 4, 3)
                        .texOffs(41, 0) .addBox(-3, -2.3457F,-3.7758F, 6,11, 7),
                PartPose.offsetAndRotation(2, -1.5171F, 4.3748F, rad(45), 0, 0));

        root.addOrReplaceChild("left_back_leg",
                CubeListBuilder.create().texOffs(35,47)
                        .addBox(-2.5F, -1.5F, -2, 4, 9, 4),
                PartPose.offset( 2.998F, -7.5F,  7));

        root.addOrReplaceChild("right_front_leg",
                CubeListBuilder.create().texOffs(52,47).mirror()
                        .addBox(-1.5F, -1.5F, -2, 4, 9, 4).mirror(false),
                PartPose.offset(-2.998F, -7.5F, -5));

        root.addOrReplaceChild("left_front_leg",
                CubeListBuilder.create().texOffs(52,47)
                        .addBox(-2.252F, -1.5F, -2, 4, 9, 4),
                PartPose.offset( 2.75F, -7.5F, -5));

        root.addOrReplaceChild("right_back_leg",
                CubeListBuilder.create().texOffs(35,47).mirror()
                        .addBox(-1.498F, -1.75F, -2, 4, 9, 4).mirror(false),
                PartPose.offset(-3, -7.25F, 7));

        return LayerDefinition.create(mesh, 128, 128);
    }

    /* ===== Animation (RenderState-based) ===== */
    @Override
    public void setupAnim(@NotNull State s) {
        head.yRot = s.headYawDeg   * Mth.DEG_TO_RAD;
        head.xRot = s.headPitchDeg * Mth.DEG_TO_RAD;

        float walk = s.limbSwing;
        float amt  = s.limbSwingAmount;

        rightFrontLeg.xRot = Mth.cos(walk * 0.6662F)          * 1.4F * amt;
        leftBackLeg .xRot  = Mth.cos(walk * 0.6662F)          * 1.4F * amt;
        leftFrontLeg.xRot  = Mth.cos(walk * 0.6662F + Mth.PI) * 1.4F * amt;
        rightBackLeg.xRot  = Mth.cos(walk * 0.6662F + Mth.PI) * 1.4F * amt;

        tail.xRot = Mth.cos(s.ageTicks * 0.2F) * 0.05F;
    }

    private static float rad(float deg) { return deg * Mth.DEG_TO_RAD; }
}
