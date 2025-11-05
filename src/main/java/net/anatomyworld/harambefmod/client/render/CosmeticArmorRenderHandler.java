package net.anatomyworld.harambefmod.client.render;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class CosmeticArmorRenderHandler {

    private CosmeticArmorRenderHandler() {}

    /**
     * MOD bus — add our cosmetic overlay layer to every player skin.
     *
     * Important: we NO LONGER remove the vanilla HumanoidArmorLayer.
     * Vanilla draws the real armor; this layer draws cosmetic overlay if present.
     */
    public static void onAddLayers(EntityRenderersEvent.AddLayers e) {
        EntityModelSet models = e.getEntityModels();
        EquipmentLayerRenderer equip = e.getContext().getEquipmentRenderer();

        for (var skin : e.getSkins()) {
            PlayerRenderer pr = e.getSkin(skin);
            if (pr != null) {
                pr.addLayer(new CosmeticWardrobeLayer(pr, equip, models));
            }
        }
    }
}
