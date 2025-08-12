package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final class Blocks {
        /** Blocks that can host Musavacca sprouting and count as an “attachment” ceiling for eggs. */
        public static final TagKey<Block> BANANA_COW_GROWTH =
                TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "banana_cow_growth"));

        private Blocks() {}
    }

    private ModTags() {}
}
