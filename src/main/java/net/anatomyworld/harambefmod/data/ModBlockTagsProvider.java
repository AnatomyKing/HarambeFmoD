package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, HarambeCore.MOD_ID);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        /* ---------- your existing vanilla tags ---------- */

        // tools
        tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.BANANA_PEARL_BLOCK.get());
        tag(net.minecraft.tags.BlockTags.NEEDS_STONE_TOOL).add(ModBlocks.BANANA_PEARL_BLOCK.get());
        tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE).add(
                ModBlocks.BANANA_COW_EGG.get(),
                ModBlocks.MUSAVACCA_STEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get(),
                ModBlocks.MUSAVACCA_PLANKS.get()
        );
        tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_HOE).add(
                ModBlocks.MUSAVACCA_FLOWER.get(),
                ModBlocks.MUSAVACCA_LEAVES.get(),
                ModBlocks.MUSAVACCA_LEAVES_CROWN.get()
        );

        // leaves/logs/planks
        tag(net.minecraft.tags.BlockTags.LEAVES).add(
                ModBlocks.MUSAVACCA_LEAVES.get(),
                ModBlocks.MUSAVACCA_LEAVES_CROWN.get()
        );
        tag(net.minecraft.tags.BlockTags.LOGS).add(
                ModBlocks.MUSAVACCA_STEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get()
        );
        tag(net.minecraft.tags.BlockTags.LOGS_THAT_BURN).add(
                ModBlocks.MUSAVACCA_STEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get()
        );
        tag(net.minecraft.tags.BlockTags.PLANKS).add(ModBlocks.MUSAVACCA_PLANKS.get());

        // crops / saplings
        tag(net.minecraft.tags.BlockTags.CROPS).add(ModBlocks.MUSAVACCA_PLANT.get());
        tag(net.minecraft.tags.BlockTags.SAPLINGS).add(ModBlocks.MUSAVACCA_SAPLING.get());

        /* ---------- YOUR CUSTOM TAG ---------- */
        // This is what actually makes the file appear under data/harambefmod/tags/blocks/
        tag(ModTags.Blocks.BANANA_COW_GROWTH).add(
                ModBlocks.MUSAVACCA_STEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get(),
                ModBlocks.MUSAVACCA_PLANKS.get()
        );
    }
}
