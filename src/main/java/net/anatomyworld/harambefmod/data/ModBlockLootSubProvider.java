package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Set;
import java.util.stream.Collectors;

public final class ModBlockLootSubProvider extends BlockLootSubProvider {

    public ModBlockLootSubProvider(HolderLookup.Provider lookup) {
        super(Set.<Item>of(), FeatureFlags.DEFAULT_FLAGS, lookup);
    }

    @Override
    protected void generate() {
        // Normal blocks
        dropSelf(ModBlocks.BANANA_PEARL_BLOCK.get());
        dropSelf(ModBlocks.MUSAVACCA_PLANKS.get());
        dropSelf(ModBlocks.MUSAVACCA_STEM.get());
        dropSelf(ModBlocks.STRIPPED_MUSAVACCA_STEM.get());
        dropSelf(ModBlocks.MUSAVACCA_FLOWER.get());


        add(ModBlocks.MUSAVACCA_LEAVES.get(),
                createSilkTouchOrShearsDispatchTable(
                        ModBlocks.MUSAVACCA_LEAVES.get(),
                        net.minecraft.world.level.storage.loot.entries.EmptyLootItem.emptyItem()
                )
        );

        add(ModBlocks.MUSAVACCA_LEAVES_CROWN.get(),
                createSilkTouchOrShearsDispatchTable(
                        ModBlocks.MUSAVACCA_LEAVES_CROWN.get(),
                        net.minecraft.world.level.storage.loot.entries.EmptyLootItem.emptyItem()
                )
        );

        // Egg drops are fully controlled by BananaCowEggBlock.spawnAfterBreak (stage items + flower).
        // Provide an EMPTY loot table to avoid double drops or wrong items.
        add(ModBlocks.BANANA_COW_EGG.get(), LootTable.lootTable());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(h -> (Block) h.value())
                .collect(Collectors.toList());
    }
}
