package net.anatomyworld.harambefmod.entity.chest.any;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.entity.AnyChestBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class AnyChestRenderer implements BlockEntityRenderer<AnyChestBlockEntity> {

    private final ModelPart lid;
    private final ModelPart latch;
    private final ModelPart base;

    // Texture on the chest atlas: assets/harambefmod/textures/entity/chest/anytomithium.png
    private static final Material CHEST_TEX = new Material(
            Sheets.CHEST_SHEET,
            ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "entity/chest/anytomithium")
    );

    public AnyChestRenderer(BlockEntityRendererProvider.Context ctx) {
        // Vanilla single chest model
        ModelPart root = ctx.bakeLayer(ModelLayers.CHEST);
        this.lid   = root.getChild("lid");
        this.latch = root.getChild("lock");
        this.base  = root.getChild("bottom");
    }

    @Override
    public void render(
            AnyChestBlockEntity be,
            float partialTicks,
            PoseStack pose,
            MultiBufferSource buf,
            int light,
            int overlay,
            Vec3 camera
    ) {
        pose.pushPose();

        // Vanilla-style transform:
        Direction dir = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        float yaw = dir.toYRot(); // SOUTH=0, WEST=90, NORTH=180, EAST=270

        pose.translate(0.5D, 0.5D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.translate(-0.5D, -0.5D, -0.5D);

        // Lid animation (same easing as vanilla)
        float open = be.getOpenNess(partialTicks);
        open = 1.0F - open;
        open = 1.0F - open * open * open;

        float lidRot = -(open * ((float) Math.PI / 2F));
        this.lid.xRot   = lidRot;
        this.latch.xRot = lidRot;

        var vc = CHEST_TEX.buffer(buf, RenderType::entityCutout);

        // Render base first (no offset)
        this.base.render(pose, vc, light, overlay);

        // Slightly nudge the lid + latch towards the camera to avoid z-fighting
        // (0.001F is tiny enough to be invisible but fixes depth conflicts)
        pose.pushPose();
        pose.translate(0.0F, 0.0F, 0.002F);
        this.lid.render(pose, vc, light, overlay);
        this.latch.render(pose, vc, light, overlay);
        pose.popPose();

        pose.popPose();
    }
}
