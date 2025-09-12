package net.anatomyworld.harambefmod.data.modelgen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.List;

public final class ItemsGen {
    private ItemsGen() {}

    /** Flat generated (parent: minecraft:item/generated). Accepts Item or Block (via ItemLike). */
    public static void flatItems(ItemModelGenerators gen, ItemLike... things) {
        for (ItemLike t : things) {
            gen.generateFlatItem(t.asItem(), ModelTemplates.FLAT_ITEM);
        }
    }
    public static void flatBlockItems(BlockModelGenerators gen, Block... blocks) {
        for (Block b : blocks) {
            gen.registerSimpleFlatItemModel(b);
        }
    }

    /** Handheld rod-like (e.g., flint_and_steel). Accepts Item or Block (via ItemLike). */
    public static void handheldRod(ItemModelGenerators gen, ItemLike... things) {
        for (ItemLike t : things) {
            gen.generateFlatItem(t.asItem(), ModelTemplates.FLAT_HANDHELD_ROD_ITEM);
        }
    }

    /** Use a custom item model JSON at models/item/... (pass a namespaced id like "modid:item/foo"). */
    public static void customItemModel(ItemModelGenerators gen, ItemLike thing, String itemModelId) {
        gen.itemModelOutput.accept(thing.asItem(), new BlockModelWrapper.Unbaked(
                ResourceLocation.parse(itemModelId), List.of()
        ));
    }

    /** Force an Item to use a *block* model JSON. */
    public static void itemUsesBlockModel(ItemModelGenerators gen, ItemLike thing, String blockModelId) {
        gen.itemModelOutput.accept(thing.asItem(), new BlockModelWrapper.Unbaked(
                ResourceLocation.parse(blockModelId), List.of()
        ));
    }

    /** Item model that renders via the block-entity renderer (e.g., chests/heads/banners). */
    public static void builtinEntity(ItemModelGenerators gen, ItemLike thing) {
        gen.itemModelOutput.accept(thing.asItem(), new BlockModelWrapper.Unbaked(
                ResourceLocation.withDefaultNamespace("builtin/entity"),
                List.of()
        ));
    }
}
