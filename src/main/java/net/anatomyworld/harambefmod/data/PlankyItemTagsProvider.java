// src/main/java/net/anatomyworld/harambefmod/data/PlankyItemTagsProvider.java
package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;
import net.minecraft.data.tags.TagsProvider;

import java.util.concurrent.CompletableFuture;

public final class PlankyItemTagsProvider extends BlockTagCopyingItemTagProvider {
    public PlankyItemTagsProvider(PackOutput output,
                                  CompletableFuture<HolderLookup.Provider> lookup,
                                  CompletableFuture<TagsProvider.TagLookup<Block>> blockTags) {
        super(output, lookup, blockTags, HarambeCore.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Mirror common block tags vanilla recipes look at (e.g., #minecraft:planks items).
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.LOGS, ItemTags.LOGS);
        copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        // If you want custom mirrors, add them here too:
        // copy(ModTags.Blocks.BANANA_PORTAL_FRAME, ModTags.Items.BANANA_PORTAL_FRAME);
    }
}
