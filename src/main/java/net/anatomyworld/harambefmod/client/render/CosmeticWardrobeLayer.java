package net.anatomyworld.harambefmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.EquipmentAsset;

public final class CosmeticWardrobeLayer extends RenderLayer<PlayerRenderState, PlayerModel> {

    private final EquipmentLayerRenderer equipment;
    private final HumanoidModel<?> inner; // Leggings
    private final HumanoidModel<?> outer; // Head/Chest/Feet

    public CosmeticWardrobeLayer(PlayerRenderer parent,
                                 EquipmentLayerRenderer equipment,
                                 EntityModelSet models) {
        super(parent);
        this.equipment = equipment;
        this.inner = new HumanoidModel<>(models.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
        this.outer = new HumanoidModel<>(models.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int packedLight,
                       PlayerRenderState state, float yaw, float partialTick) {
        // Keep our model transforms in sync with the player's current animation
        PlayerModel parent = this.getParentModel();
        copyPose(parent, inner);
        copyPose(parent, outer);

        // Render each configured cosmetic stack from render state
        renderSlot(pose, buf, packedLight, state.getRenderData(CosmeticWardrobeRenderData.COS_HEAD),
                EquipmentSlot.HEAD, EquipmentClientInfo.LayerType.HUMANOID);
        renderSlot(pose, buf, packedLight, state.getRenderData(CosmeticWardrobeRenderData.COS_CHEST),
                EquipmentSlot.CHEST, EquipmentClientInfo.LayerType.HUMANOID);
        renderSlot(pose, buf, packedLight, state.getRenderData(CosmeticWardrobeRenderData.COS_LEGS),
                EquipmentSlot.LEGS, EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS);
        renderSlot(pose, buf, packedLight, state.getRenderData(CosmeticWardrobeRenderData.COS_FEET),
                EquipmentSlot.FEET, EquipmentClientInfo.LayerType.HUMANOID);
    }

    private void renderSlot(PoseStack pose, MultiBufferSource buf, int light,
                            ItemStack stack, EquipmentSlot slot, EquipmentClientInfo.LayerType layerType) {
        if (stack == null || stack.isEmpty()) return;

        // Validate equippable + get the asset id that defines how to render this armor
        Equippable eq = stack.get(DataComponents.EQUIPPABLE);
        if (eq == null || eq.slot() != slot) return;
        ResourceKey<EquipmentAsset> assetKey = eq.assetId().orElse(null);
        if (assetKey == null) return;

        // Pick the correct armor model and set per-slot visibility like vanilla
        HumanoidModel<?> model = (slot == EquipmentSlot.LEGS) ? inner : outer;
        setPartVisibility(model, slot);

        // Draw all equipment layers (base/overlay/trim/tints) for this asset
        equipment.renderLayers(layerType, assetKey, model, stack, pose, buf, light, null);
    }

    /** Match vanilla HumanoidArmorLayer visibility per slot. */
    private static void setPartVisibility(HumanoidModel<?> model, EquipmentSlot slot) {
        model.setAllVisible(false);
        switch (slot) {
            case HEAD -> { model.head.visible = true; model.hat.visible = true; }
            case CHEST -> { model.body.visible = true; model.rightArm.visible = true; model.leftArm.visible = true; }
            case LEGS  -> { model.body.visible = true; model.rightLeg.visible = true; model.leftLeg.visible = true; }
            case FEET  -> { model.rightLeg.visible = true; model.leftLeg.visible = true; }
        }
    }

    /** Copy transforms so the cosmetic follows the current animation pose. */
    private static void copyPose(PlayerModel from, HumanoidModel<?> to) {
        to.head.copyFrom(from.head);
        to.hat.copyFrom(from.hat);
        to.body.copyFrom(from.body);
        to.rightArm.copyFrom(from.rightArm);
        to.leftArm.copyFrom(from.leftArm);
        to.rightLeg.copyFrom(from.rightLeg);
        to.leftLeg.copyFrom(from.leftLeg);
    }
}
