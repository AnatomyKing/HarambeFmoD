// file: src/main/java/net/anatomyworld/harambefmod/entity/mob/bananacow/clientmodel/BananaCowModel.java
package net.anatomyworld.harambefmod.entity.mob.bananacow.clientmodel;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class BananaCowModel extends EntityModel<BananaCowModel.State> {

    public static class State extends LivingEntityRenderState {
        public float headYawRad;
        public float headPitchRad;

        public float limbSwing;
        public float limbSwingAmount;
        public float ageTicks;
    }

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "banana_cow"),
                    "main"
            );

    private final ModelPart hHead;
    private final ModelPart tail;
    private final ModelPart rightFrontLeg, leftFrontLeg, rightBackLeg, leftBackLeg;

    private final float headBaseXRot;
    private final float tailBaseXRot;

    public BananaCowModel(ModelPart bakedRoot) {
        super(bakedRoot);

        ModelPart bananacow = bakedRoot.getChild("bananacow");

        this.hHead = bananacow.getChild("h_head");

        ModelPart torso = bananacow.getChild("torso");
        this.tail = torso.getChild("tail");

        this.rightBackLeg = bananacow.getChild("right_back_leg");
        this.leftBackLeg = bananacow.getChild("left_back_leg");
        this.rightFrontLeg = bananacow.getChild("right_front_leg");
        this.leftFrontLeg = bananacow.getChild("left_front_leg");

        this.headBaseXRot = this.hHead.xRot;
        this.tailBaseXRot = this.tail.xRot;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bananacow = partdefinition.addOrReplaceChild(
                "bananacow",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 25.0F, -1.0F)
        );

        PartDefinition h_head = bananacow.addOrReplaceChild(
                "h_head",
                CubeListBuilder.create()
                        .texOffs(39, 31).addBox(-4.0F, -4.6159F, -7.0936F, 8.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 57).addBox(-3.0F, 0.3841F, -8.0936F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 57).addBox(4.0F, -4.6159F, -5.0936F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(5, 57).addBox(-5.0F, -4.6159F, -5.0936F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -13.5183F, -8.3348F, -0.3927F, 0.0F, 0.0F)
        );

        PartDefinition torso = bananacow.addOrReplaceChild(
                "torso",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-1.0F, -19.0F, 2.0F, 1.5708F, 0.0F, 0.0F)
        );

        PartDefinition body = torso.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.5F, -9.0F, -5.0F, 11.0F, 19.0F, 11.0F, new CubeDeformation(0.03F)),
                PartPose.offset(1.0F, -1.0F, -6.0F)
        );

        PartDefinition tail = torso.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                        .texOffs(0, 31).addBox(-4.5F, -1.3965F, -4.98F, 9.0F, 15.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 7.25F, -5.75F, 0.3927F, 0.0F, 0.0F)
        );

        PartDefinition tip = tail.addOrReplaceChild(
                "tip",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-2.0F, 14.2535F, 0.5186F, 0.6981F, 0.0F, 0.0F)
        );

        tip.addOrReplaceChild(
                "cube_r1",
                CubeListBuilder.create()
                        .texOffs(45, 21).addBox(-4.0F, 4.6543F, -0.7758F, 7.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.5F, 1.7463F, 4.2942F, -0.3927F, 0.0F, 0.0F)
        );

        tip.addOrReplaceChild(
                "cube_r2",
                CubeListBuilder.create()
                        .texOffs(45, 0).addBox(-4.0F, -2.3457F, -3.7758F, 7.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.5F, -0.1671F, -0.3252F, -0.3927F, 0.0F, 0.0F)
        );

        bananacow.addOrReplaceChild(
                "right_back_leg",
                CubeListBuilder.create()
                        .texOffs(39, 49).mirror().addBox(-2.248F, -1.75F, -2.0F, 4.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
                        .mirror(false),
                PartPose.offset(-3.0F, -7.25F, 7.0F)
        );

        bananacow.addOrReplaceChild(
                "left_back_leg",
                CubeListBuilder.create()
                        .texOffs(39, 49).addBox(-1.75F, -1.5F, -2.0F, 4.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.998F, -7.5F, 7.0F)
        );

        bananacow.addOrReplaceChild(
                "right_front_leg",
                CubeListBuilder.create()
                        .texOffs(39, 49).mirror().addBox(-2.25F, -1.5F, -2.0F, 4.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
                        .mirror(false),
                PartPose.offset(-2.998F, -7.5F, -5.0F)
        );

        bananacow.addOrReplaceChild(
                "left_front_leg",
                CubeListBuilder.create()
                        .texOffs(39, 49).addBox(-1.502F, -1.5F, -2.0F, 4.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.75F, -7.5F, -5.0F)
        );

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(@NotNull State s) {
        hHead.yRot = s.headYawRad;
        hHead.xRot = headBaseXRot + s.headPitchRad;

        if (s.isBaby) {
            hHead.xScale = hHead.yScale = hHead.zScale = 1.40F;
        } else {
            hHead.xScale = hHead.yScale = hHead.zScale = 1.0F;
        }

        float walk = s.limbSwing;
        float amt = s.limbSwingAmount;

        rightFrontLeg.xRot = Mth.cos(walk * 0.6662F) * 1.4F * amt;
        leftBackLeg.xRot   = Mth.cos(walk * 0.6662F) * 1.4F * amt;
        leftFrontLeg.xRot  = Mth.cos(walk * 0.6662F + Mth.PI) * 1.4F * amt;
        rightBackLeg.xRot  = Mth.cos(walk * 0.6662F + Mth.PI) * 1.4F * amt;

        tail.xRot = tailBaseXRot + (Mth.cos(s.ageTicks * 0.2F) * 0.05F);
    }
}
