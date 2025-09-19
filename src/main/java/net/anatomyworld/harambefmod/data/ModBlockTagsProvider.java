package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
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
                ModBlocks.BANANA_BLOCK.get(),
                ModBlocks.MUSAVACCA_PLANKS.get(),
                ModBlocks.BELMONT_PLANKS.get(),
                ModBlocks.DYNASTY_PLANKS.get(),
                ModBlocks.IMPERIUM_PLANKS.get(),
                ModBlocks.MISCHIEF_PLANKS.get(),
                ModBlocks.STRIPPED_BELMONT_LOG.get(),
                ModBlocks.BELMONT_LOG.get(),
                ModBlocks.STRIPPED_DYNASTY_LOG.get(),
                ModBlocks.DYNASTY_LOG.get(),
                ModBlocks.STRIPPED_IMPERIUM_LOG.get(),
                ModBlocks.IMPERIUM_LOG.get(),
                ModBlocks.STRIPPED_MISCHIEF_LOG.get(),
                ModBlocks.MISCHIEF_LOG.get(),
                ModBlocks.MUSAVACCA_PSEUDOSTEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_PSEUDOSTEM.get(),
                ModBlocks.STRIPPED_BELMONT_WOOD.get(),
                ModBlocks.BELMONT_WOOD.get(),
                ModBlocks.STRIPPED_DYNASTY_WOOD.get(),
                ModBlocks.DYNASTY_WOOD.get(),
                ModBlocks.STRIPPED_IMPERIUM_WOOD.get(),
                ModBlocks.IMPERIUM_WOOD.get(),
                ModBlocks.STRIPPED_MISCHIEF_WOOD.get(),
                ModBlocks.MISCHIEF_WOOD.get()

        );
        tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_HOE).add(
                ModBlocks.MUSAVACCA_FLOWER.get(),
                ModBlocks.MUSAVACCA_LEAVES.get(),
                ModBlocks.MUSAVACCA_LEAVES_CROWN.get()
        );

        tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL).add(
                ModBlocks.BANANA_CREAM_STONE.get(),
                ModBlocks.CHOCO_CREAM_STONE.get(),
                ModBlocks.VANILLA_CREAM_STONE.get(),
                ModBlocks.STRAWBERRY_CREAM_STONE.get(),
                ModBlocks.CAROTENE_GRASS_BLOCK.get(),
                ModBlocks.BELMONT_GRASS_BLOCK.get(),
                ModBlocks.DYNASTY_GRASS_BLOCK.get(),
                ModBlocks.IMPERIUM_GRASS_BLOCK.get(),
                ModBlocks.MISCHIEF_GRASS_BLOCK.get()
        );

        // leaves/logs/planks
        tag(net.minecraft.tags.BlockTags.LEAVES).add(
                ModBlocks.MUSAVACCA_LEAVES.get(),
                ModBlocks.MUSAVACCA_LEAVES_CROWN.get(),
                ModBlocks.BELMONT_LEAVES.get(),
                ModBlocks.DYNASTY_LEAVES.get(),
                ModBlocks.IMPERIUM_LEAVES.get(),
                ModBlocks.MISCHIEF_LEAVES.get()
        );

        tag(BlockTags.MOSS_REPLACEABLE).add(
                ModBlocks.CAROTENE_GRASS_BLOCK.get(),
                ModBlocks.BELMONT_GRASS_BLOCK.get(),
                ModBlocks.DYNASTY_GRASS_BLOCK.get(),
                ModBlocks.IMPERIUM_GRASS_BLOCK.get(),
                ModBlocks.MISCHIEF_GRASS_BLOCK.get()
        );

        tag(net.minecraft.tags.BlockTags.LOGS).add(
                ModBlocks.MUSAVACCA_STEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get(),

                ModBlocks.MUSAVACCA_PSEUDOSTEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_PSEUDOSTEM.get(),

                ModBlocks.STRIPPED_BELMONT_LOG.get(),
                ModBlocks.BELMONT_LOG.get(),
                ModBlocks.STRIPPED_DYNASTY_LOG.get(),
                ModBlocks.DYNASTY_LOG.get(),
                ModBlocks.STRIPPED_IMPERIUM_LOG.get(),
                ModBlocks.IMPERIUM_LOG.get(),
                ModBlocks.STRIPPED_MISCHIEF_LOG.get(),
                ModBlocks.MISCHIEF_LOG.get(),
                ModBlocks.STRIPPED_BELMONT_WOOD.get(),
                ModBlocks.BELMONT_WOOD.get(),
                ModBlocks.STRIPPED_DYNASTY_WOOD.get(),
                ModBlocks.DYNASTY_WOOD.get(),
                ModBlocks.STRIPPED_IMPERIUM_WOOD.get(),
                ModBlocks.IMPERIUM_WOOD.get(),
                ModBlocks.STRIPPED_MISCHIEF_WOOD.get(),
                ModBlocks.MISCHIEF_WOOD.get()

        );
        tag(net.minecraft.tags.BlockTags.LOGS_THAT_BURN).add(
                ModBlocks.MUSAVACCA_STEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get(),

                ModBlocks.MUSAVACCA_PSEUDOSTEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_PSEUDOSTEM.get(),

                ModBlocks.STRIPPED_BELMONT_LOG.get(),
                ModBlocks.BELMONT_LOG.get(),
                ModBlocks.STRIPPED_DYNASTY_LOG.get(),
                ModBlocks.DYNASTY_LOG.get(),
                ModBlocks.STRIPPED_IMPERIUM_LOG.get(),
                ModBlocks.IMPERIUM_LOG.get(),
                ModBlocks.STRIPPED_MISCHIEF_LOG.get(),
                ModBlocks.MISCHIEF_LOG.get(),
                ModBlocks.STRIPPED_BELMONT_WOOD.get(),
                ModBlocks.BELMONT_WOOD.get(),
                ModBlocks.STRIPPED_DYNASTY_WOOD.get(),
                ModBlocks.DYNASTY_WOOD.get(),
                ModBlocks.STRIPPED_IMPERIUM_WOOD.get(),
                ModBlocks.IMPERIUM_WOOD.get(),
                ModBlocks.STRIPPED_MISCHIEF_WOOD.get(),
                ModBlocks.MISCHIEF_WOOD.get()
        );
        tag(BlockTags.PLANKS).add(
                ModBlocks.MUSAVACCA_PLANKS.get(),
                ModBlocks.BELMONT_PLANKS.get(),
                ModBlocks.DYNASTY_PLANKS.get(),
                ModBlocks.IMPERIUM_PLANKS.get(),
                ModBlocks.MISCHIEF_PLANKS.get()


                );


        // crops / saplings
        tag(BlockTags.CROPS).add(ModBlocks.MUSAVACCA_PLANT.get());
        tag(BlockTags.SAPLINGS).add(
                ModBlocks.MUSAVACCA_SAPLING.get(),
                ModBlocks.BELMONT_SAPLING.get(),
                ModBlocks.DYNASTY_SAPLING.get(),
                ModBlocks.IMPERIUM_SAPLING.get(),
                ModBlocks.MISCHIEF_SAPLING.get()
        );

        // existing custom example
        tag(ModTags.Blocks.BANANA_COW_GROWTH).add(
                ModBlocks.MUSAVACCA_STEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get(),
                ModBlocks.MUSAVACCA_PLANKS.get(),
                ModBlocks.MUSAVACCA_PSEUDOSTEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_PSEUDOSTEM.get()
        );

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.PEARLIDIAN.get(),
                ModBlocks.ANYTOMITHIUM_CHEST.get());
        tag(BlockTags.NEEDS_DIAMOND_TOOL).add(ModBlocks.PEARLIDIAN.get());

        /* ---------- NEW: portal frame tags ---------- */
        // Intra-dimensional frame blocks (current behavior). We include the Pearl Block by default.
        tag(ModTags.Blocks.BANANA_PORTAL_FRAME).add(ModBlocks.BANANA_PEARL_BLOCK.get());

        // Inter-dimensional frame blocks (empty by default — add via datapack or here later)
        tag(ModTags.Blocks.BANANA_PORTAL_FRAME_INTER).add(ModBlocks.PEARLIDIAN.get());


        tag(BlockTags.DIRT).add(
                ModBlocks.CAROTENE_GRASS_BLOCK.get(),
                ModBlocks.BELMONT_GRASS_BLOCK.get(),
                ModBlocks.DYNASTY_GRASS_BLOCK.get(),
                ModBlocks.IMPERIUM_GRASS_BLOCK.get(),
                ModBlocks.MISCHIEF_GRASS_BLOCK.get()
                );

    }
}
