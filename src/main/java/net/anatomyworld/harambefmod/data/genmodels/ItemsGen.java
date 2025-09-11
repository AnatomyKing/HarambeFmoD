package net.anatomyworld.harambefmod.data.genmodels;

import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collections;

import static net.anatomyworld.harambefmod.data.genmodels.ModelUtil.rl;

/** Item-model helpers (Client Items 1.21+) */
public final class ItemsGen {
    private ItemsGen() {}

    /** Flat generated (layer0 = textures/item/<id>.png) */
    public static void flat(ItemModelGenerators gen, Item... items) {
        for (Item i : items) gen.generateFlatItem(i, ModelTemplates.FLAT_ITEM);
    }

    /** Handheld (rod) – goed voor “flint_and_steel”-achtige items. */
    public static void handheldRod(ItemModelGenerators gen, Item... items) {
        for (Item i : items) gen.generateFlatItem(i, ModelTemplates.FLAT_HANDHELD_ROD_ITEM);
    }

    /** Custom 3D item: wijs naar models/item/<name>.json die jij aanlevert. */
    public static void customItemModel(ItemModelGenerators gen, Item item, String itemModelPath) {
        gen.itemModelOutput.accept(item,
                new BlockModelWrapper.Unbaked(
                        // bijv: "harambefmod:item/anyphone_3d"
                        ResourceLocation.parse(itemModelPath),
                        Collections.emptyList()
                )
        );
    }

    /** Custom 3D item dat naar een blockmodel wijst (models/block/<name>.json) */
    public static void itemUsesBlockModel(ItemModelGenerators gen, Item item, String blockModelPath) {
        gen.itemModelOutput.accept(item,
                new BlockModelWrapper.Unbaked(
                        // bijv: "harambefmod:block/banana_pearl_block"
                        rl("minecraft", blockModelPath), // of jouw eigen namespace
                        Collections.emptyList()
                )
        );
    }
}
