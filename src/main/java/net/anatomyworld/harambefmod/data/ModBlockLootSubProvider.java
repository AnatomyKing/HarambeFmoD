package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Set;
import java.util.stream.Collectors;


public final class ModBlockLootSubProvider extends BlockLootSubProvider {

    /** The registry lookup is injected by the LootTableProvider. */
    public ModBlockLootSubProvider(HolderLookup.Provider lookup) {
        super(Set.<Item>of(),                        // no explosion-immune blocks
                FeatureFlags.DEFAULT_FLAGS,            // all vanilla flags enabled
                lookup);
    }

    /** Actual loot-table contents. */
    @Override
    protected void generate() {
        
        dropSelf(ModBlocks.BANANA_PEARL_BLOCK.get());


        add(ModBlocks.BANANA_COW_EGG.get(),
                createSilkTouchOnlyTable(ModBlocks.BANANA_COW_EGG.get()));
    }

    /** Validation list – every registered block must appear here. */
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries()         // DeferredHolder<?>
                .stream()
                .map(entry -> (Block) entry.value()) // cast to Block
                .collect(Collectors.toList());
    }
}
