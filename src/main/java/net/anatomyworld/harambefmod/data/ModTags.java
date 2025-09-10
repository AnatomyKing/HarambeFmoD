package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final class Blocks {

        // Existing
        public static final TagKey<Block> BANANA_COW_GROWTH =
                TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "banana_cow_growth"));

        // NEW: portal frame tags
        public static final TagKey<Block> BANANA_PORTAL_FRAME =
                TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "banana_portal_frame"));

        public static final TagKey<Block> BANANA_PORTAL_FRAME_INTER =
                TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "banana_portal_frame_inter"));

        private Blocks() {}
    }

    private ModTags() {}
}
