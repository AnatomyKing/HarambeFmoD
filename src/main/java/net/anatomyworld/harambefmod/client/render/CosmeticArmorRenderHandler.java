package net.anatomyworld.harambefmod.client.render;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.lang.reflect.Field;
import java.util.List;

public final class CosmeticArmorRenderHandler {

    private CosmeticArmorRenderHandler() {}

    /** MOD bus — add our layer to every player skin. */
    public static void onAddLayers(EntityRenderersEvent.AddLayers e) {
        EntityModelSet models = e.getEntityModels();
        EquipmentLayerRenderer equip = e.getContext().getEquipmentRenderer();

        for (var skin : e.getSkins()) {
            PlayerRenderer pr = e.getSkin(skin);
            if (pr != null) {
                // 1) Remove vanilla armor layer so we fully control armor rendering.
                removeVanillaHumanoidArmorLayer(pr);

                // 2) Add our single layer which draws: cosmetic if present, else real armor.
                pr.addLayer(new CosmeticWardrobeLayer(pr, equip, models));
            }
        }
    }

    /** Reflectively remove the vanilla HumanoidArmorLayer from LivingEntityRenderer#layers. */
    @SuppressWarnings({"unchecked","rawtypes"})
    private static void removeVanillaHumanoidArmorLayer(PlayerRenderer pr) {
        try {
            Field layersF = LivingEntityRenderer.class.getDeclaredField("layers"); // Mojmap name
            layersF.setAccessible(true);
            List<RenderLayer> layers = (List<RenderLayer>) layersF.get(pr);
            layers.removeIf(layer -> layer instanceof HumanoidArmorLayer);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            // Fail-soft: if removal fails, vanilla armor may double-render with our fallback.
            // You can log here if you like.
        }
    }
}
