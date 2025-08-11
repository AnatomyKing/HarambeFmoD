/*  =======================================================================
    File: src/main/java/net/anatomyworld/harambefd/entity/bananacow/clientmodel/BananaCowModel.java
    ======================================================================= */
package net.anatomyworld.harambefmod.entity.bananacow.clientmodel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.entity.bananacow.BananaCow;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public class BananaCowModel extends EntityModel<BananaCow> {

    /* ------------------------------------------------------------------ */
    /*  Layer ID (used in RegisterLayerDefinitions & renderer)            */
    /* ------------------------------------------------------------------ */
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(
                            HarambeCore.MOD_ID, "banana_cow"),
                    "main");

    /* animated parts */
    private final ModelPart root, head, tail;
    private final ModelPart rightFrontLeg, leftFrontLeg, rightBackLeg, leftBackLeg;

    /* ctor – baked root is supplied by ctx.bakeLayer(...) in renderer */
    public BananaCowModel(ModelPart bakedRoot) {
        this.root          = bakedRoot.getChild("root");
        this.head          = root.getChild("head");
        this.tail          = root.getChild("body").getChild("tail");
        this.leftBackLeg   = root.getChild("left_back_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg  = root.getChild("left_front_leg");
        this.rightBackLeg  = root.getChild("right_back_leg");
    }

    /* ------------------------------------------------------------------ */
    /*  Geometry – pasted from Blockbench export                          */
    /* ------------------------------------------------------------------ */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        /* root pivot at (0,24,-1) */
        PartDefinition root = part.addOrReplaceChild("root",
                CubeListBuilder.create(),
                PartPose.offset(0, 24, -1));

        /* head ---------------------------------------------------------- */
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create(),
                PartPose.offset(0, -12.5F, -7));

        head.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(35, 29)
                        .addBox(-4, -4.1F, -8.5F, 8, 8, 9),
                PartPose.offsetAndRotation(0, -0.5796F, 0.2059F,
                        rad(-22.5F), 0, 0));

        /* body (rolled 90 °) ------------------------------------------- */
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-1, -19, 2, rad(90), 0, 0));

        body.addOrReplaceChild("torso",
                CubeListBuilder.create()
                        .texOffs(0, 0) .addBox(-5, -9, -5, 10, 18, 10)
                        .texOffs(0,54).addBox(-2,  0, -6,  4,  4,  1),
                PartPose.offset(1, -1, -6));

        /* tail ---------------------------------------------------------- */
        PartDefinition tail = body.addOrReplaceChild("tail",
                CubeListBuilder.create(),
                PartPose.offset(1, 7.25F, -5.75F));

        tail.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(0, 29)
                        .addBox(-4, -2.5F, -4.5F, 8, 15, 9),
                PartPose.offsetAndRotation(0, 1.0035F, -0.2314F,
                        rad(22.5F), 0, 0));

        PartDefinition tip = tail.addOrReplaceChild("tip",
                CubeListBuilder.create(),
                PartPose.offset(-2, 14.2535F, 0.5186F));

        tip.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                        .texOffs(41,19).addBox(-3,  4.6543F, 3.2242F, 6, 4, 3)
                        .texOffs(41, 0) .addBox(-3, -2.3457F,-3.7758F, 6,11, 7),
                PartPose.offsetAndRotation(2, -1.5171F, 4.3748F,
                        rad(45), 0, 0));

        /* legs ---------------------------------------------------------- */
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

    /* ------------------------------------------------------------------ */
    /* Animation – plus baby-head scaling                                 */
    /* ------------------------------------------------------------------ */
    @Override
    public void setupAnim(@NotNull BananaCow entity,
                          float walk, float walkAmount,
                          float age,  float headYaw, float headPitch) {

        /* look + walk */
        head.yRot = headYaw   * DEG;
        head.xRot = headPitch * DEG;

        rightFrontLeg.xRot = (float) Math.cos(walk * 0.6662F)       * 1.4F * walkAmount;
        leftBackLeg .xRot  = (float) Math.cos(walk * 0.6662F)       * 1.4F * walkAmount;
        leftFrontLeg.xRot  = (float) Math.cos(walk * 0.6662F + PI)  * 1.4F * walkAmount;
        rightBackLeg.xRot  = (float) Math.cos(walk * 0.6662F + PI)  * 1.4F * walkAmount;

        tail.xRot = (float) Math.cos(age * 0.2F) * 0.05F;

        /* -------------------------------------------------------------- */
        /* baby head - vanilla enlarges head 1.4× relative to the body    */
        /* (renderer already scales the whole calf 0.5×)                 */
        /* -------------------------------------------------------------- */
    if (entity.isBaby()) {
        head.xScale = head.yScale = head.zScale = 1.40F;   // big cute head
    } else {
       head.xScale = head.yScale = head.zScale = 1.0F;    // reset for adults
    }}

    /* ------------------------------------------------------------------ */
    /* Render (packedRGB = colour multiplier)                             */
    /* ------------------------------------------------------------------ */
    @Override
    public void renderToBuffer(@NotNull PoseStack ps, @NotNull VertexConsumer vc,
                               int packedLight, int packedOverlay, int packedRGB) {
        root.render(ps, vc, packedLight, packedOverlay);
    }

    /* helpers */
    private static float rad(float deg) { return deg * PI / 180F; }
    private static final float PI  = (float) Math.PI;
    private static final float DEG = PI / 180F;
}
