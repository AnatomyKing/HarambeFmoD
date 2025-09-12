package net.anatomyworld.harambefmod.client.render;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class CosmeticArmorRenderHandler {

    private CosmeticArmorRenderHandler() {}

    /** MOD bus — add our layer to every player skin. */
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
