package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/** Block tags provider (1.21.8 signature — no ExistingFileHelper, no includeServer). */
public final class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, HarambeCore.MOD_ID);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        // tools
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.BANANA_PEARL_BLOCK.get());
        tag(BlockTags.NEEDS_STONE_TOOL).add(ModBlocks.BANANA_PEARL_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(
                ModBlocks.BANANA_COW_EGG.get(),
                ModBlocks.MUSAVACCA_STEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get(),
                ModBlocks.MUSAVACCA_PLANKS.get()
        );
        tag(BlockTags.MINEABLE_WITH_HOE).add(
                ModBlocks.MUSAVACCA_FLOWER.get(),
                ModBlocks.MUSAVACCA_LEAVES.get(),
                ModBlocks.MUSAVACCA_LEAVES_CROWN.get()
        );

        // leaves/logs/planks
        tag(BlockTags.LEAVES).add(
                ModBlocks.MUSAVACCA_LEAVES.get(),
                ModBlocks.MUSAVACCA_LEAVES_CROWN.get()
        );
        tag(BlockTags.LOGS).add(
                ModBlocks.MUSAVACCA_STEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get()
        );
        tag(BlockTags.LOGS_THAT_BURN).add(
                ModBlocks.MUSAVACCA_STEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get()
        );
        tag(BlockTags.PLANKS).add(ModBlocks.MUSAVACCA_PLANKS.get());

        // crops / saplings
        tag(BlockTags.CROPS).add(ModBlocks.MUSAVACCA_PLANT.get());
        tag(BlockTags.SAPLINGS).add(ModBlocks.MUSAVACCA_SAPLING.get());
    }
}
