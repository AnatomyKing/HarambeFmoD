// AnyChestRenderer.java
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
    private final ModelPart lid, latch, base;

    private static final Material CHEST_TEX = new Material(
            Sheets.CHEST_SHEET,
            ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "entity/chest/anytomithium")
    );

    public AnyChestRenderer(BlockEntityRendererProvider.Context ctx) {
        var root = ctx.bakeLayer(ModelLayers.CHEST); // single-chest layer
        this.lid   = root.getChild("lid");
        this.latch = root.getChild("lock");
        this.base  = root.getChild("bottom");
    }

    // ✅ 1.21.6+ signature includes camera Vec3
    @Override
    public void render(AnyChestBlockEntity be, float partialTicks, PoseStack pose, MultiBufferSource buf,
                       int light, int overlay, Vec3 camera) {
        pose.pushPose();

        // center on the block
        pose.translate(0.5F, 1.5F, 0.5F);

        // flip Minecraft's model space to GL (flip Y only; see next section)
        pose.scale(1.0F, -1.0F, 1.0F);

        // rotate to match the block's facing
        Direction dir = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        float yaw = switch (dir) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case WEST  -> 90f;
            case EAST  -> -90f;
            default    -> 0f;
        };
        pose.mulPose(Axis.YP.rotationDegrees(yaw));

        float open = be.getOpenNess(partialTicks);
        open = 1.0F - open;
        open = 1.0F - open * open * open; // vanilla easing

        lid.xRot   = -(open * ((float)Math.PI / 2F));
        latch.xRot = lid.xRot;

        var vb = CHEST_TEX.buffer(buf, RenderType::entityCutout);
        lid.render(pose, vb, light, overlay);
        latch.render(pose, vb, light, overlay);
        base.render(pose, vb, light, overlay);

        pose.popPose();
    }
}
