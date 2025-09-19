package net.anatomyworld.harambefmod.worldgen;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class PatchProfiles {

    /** Tree builders you can pick per profile (or set to null to disable trees). */
    public enum TreeStyle { BELMONT, DYNASTY, IMPERIUM, MISCHIEF }

    public record Profile(
            String suffix,

            // patch surface palette
            Block coreTop,
            Block edgeTop,
            Block underBlock,
            Block shortGrassSwap,

            // --- TREE CONFIG (all optional-ish) ---
            TreeStyle treeStyle,     // NULL => no trees on patches
            int treeRarity,          // average once every N chunks (<=0 disables)
            int treeCountPerRun,     // how many to try per placement run (<=0 disables)

            // --- RED SAND OVERLAY CONFIG ---
            boolean enableSandyRedSand, // toggle the beach/desert red-sand overlay on patches
            Block sandyRedSandBlock,    // which block to use when enabled (e.g., Blocks.RED_SAND)

            // Optional: if non-null, use this noise settings id as-is for the LevelStem
            ResourceLocation customNoiseSettingsId
    ) {}


    public static final Profile HARAMBE_DEFAULT = new Profile(
            "harambe_default",
            ModBlocks.CAROTENE_GRASS_BLOCK.get(),
            Blocks.PODZOL,
            ModBlocks.CHOCO_CREAM_STONE.get(),
            ModBlocks.CAROTENE_SHORT_GRASS.get(),
            null, 0, 0,

            // red sand overlay enabled, using vanilla red sand
            true, Blocks.RED_SAND,

            // noise settings: null => build NORMAL overworld + prepend our surface rules
            null
    );

    public static final Profile BELNADES_DEFAULT = new Profile(
            "belnades_default",
            ModBlocks.BELMONT_GRASS_BLOCK.get(),
            ModBlocks.BELMONT_GRASS_BLOCK.get(),
            Blocks.DIRT,
            ModBlocks.BELMONT_SHORT_GRASS.get(),
            TreeStyle.BELMONT, 21, 1,

            // red sand overlay enabled, using vanilla red sand
            true, Blocks.LIME_CONCRETE_POWDER,

            // noise settings: null => build NORMAL overworld + prepend our surface rules
            null
    );

    public static final Profile DYNASTIRIUM_DEFAULT = new Profile(
            "dynastirium_default",
            ModBlocks.DYNASTY_GRASS_BLOCK.get(),
            ModBlocks.DYNASTY_GRASS_BLOCK.get(),
            Blocks.DIRT,
            ModBlocks.DYNASTY_SHORT_GRASS.get(),
            TreeStyle.DYNASTY, 20, 1,

            // red sand overlay enabled, using vanilla red sand
            true, Blocks.BLACK_CONCRETE_POWDER,

            // noise settings: null => build NORMAL overworld + prepend our surface rules
            null
    );

    public static final Profile IMPERO_DEFAULT = new Profile(
            "impero_default",
            ModBlocks.IMPERIUM_GRASS_BLOCK.get(),
            ModBlocks.IMPERIUM_GRASS_BLOCK.get(),
            Blocks.DIRT,
            ModBlocks.IMPERIUM_SHORT_GRASS.get(),
            TreeStyle.IMPERIUM, 18, 1,

            // red sand overlay enabled, using vanilla red sand
            true, Blocks.BLUE_CONCRETE_POWDER,

            // noise settings: null => build NORMAL overworld + prepend our surface rules
            ResourceLocation.fromNamespaceAndPath("harambefmod", "amplified_impero_carotene_merged")
    );

    public static final Profile MARCHELUS_DEFAULT = new Profile(
            "marchelus_default",
            ModBlocks.MISCHIEF_GRASS_BLOCK.get(),
            ModBlocks.MISCHIEF_GRASS_BLOCK.get(),
            Blocks.DIRT,
            ModBlocks.MISCHIEF_SHORT_GRASS.get(),
            TreeStyle.MISCHIEF, 24, 1,

            // red sand overlay enabled, using vanilla red sand
            true, Blocks.PURPLE_CONCRETE_POWDER,

            // noise settings: null => build NORMAL overworld + prepend our surface rules
            null
    );

    public static final Profile[] ALL = new Profile[] {HARAMBE_DEFAULT, BELNADES_DEFAULT, DYNASTIRIUM_DEFAULT, IMPERO_DEFAULT, MARCHELUS_DEFAULT};

    private PatchProfiles() {}
}
