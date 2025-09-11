package net.anatomyworld.harambefmod.data.genmodels;

import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.List;

public final class ItemsGen {
    private ItemsGen() {}

    /** Flat generated (layer0 = textures/item/<id>.png) */
    public static void flat(ItemModelGenerators gen, Item... items) {
        for (Item i : items) gen.generateFlatItem(i, ModelTemplates.FLAT_ITEM);
    }

    /** Handheld rod-like (flint_and_steel etc.) */
    public static void handheldRod(ItemModelGenerators gen, Item... items) {
        for (Item i : items) gen.generateFlatItem(i, ModelTemplates.FLAT_HANDHELD_ROD_ITEM);
    }

    /** Use a custom model JSON at models/item/... (pass a namespaced id string) */
    public static void customItemModel(ItemModelGenerators gen, Item item, String itemModelId) {
        // itemModelId example: "harambefmod:item/anyphone"
        gen.itemModelOutput.accept(item, new BlockModelWrapper.Unbaked(
                ResourceLocation.parse(itemModelId), List.of()));
    }

    public static void itemUsesBlockModel(ItemModelGenerators gen, Item item, String blockModelId) {
        gen.itemModelOutput.accept(item, new BlockModelWrapper.Unbaked(
                ResourceLocation.parse(blockModelId), List.of()));
    }

}
