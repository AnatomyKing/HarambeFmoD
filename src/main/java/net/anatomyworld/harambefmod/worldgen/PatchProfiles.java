package net.anatomyworld.harambefmod.worldgen;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Modular per-profile configuration for footprint patches and optional trees.
 *
 * New per-profile knobs:
 * - patchSize      : scales window widths (overall footprint size). 1.0 = baseline.
 * - patchCoverage  : scales gate width (how often patches appear).   1.0 = baseline.
 */
public final class PatchProfiles {

    /** Tree builders you can pick per profile (or set to null to disable trees). */
    public enum TreeStyle { BELMONT, DYNASTY, IMPERIUM, MISCHIEF }

    public record Profile(
            String suffix,

            // Patch surface palette (top/edge/underlay) + what SHORT_GRASS becomes on the patch.
            Block patchCoreTop,
            Block patchEdgeTop,
            Block patchUnderlay,
            Block shortGrassVariant,

            // --- PATCH TUNING (simple!) ---
            float patchSize,         // 1.0 baseline (0.6–1.6 sensible)
            float patchCoverage,     // 1.0 baseline (0.6–1.5 sensible)

            // --- TREE CONFIG (all optional-ish) ---
            TreeStyle treeStyle,     // NULL => no trees on patches
            int treeRarity,          // average once every N chunks (<=0 disables)
            int treeCountPerRun,     // how many to try per placement run (<=0 disables)

            // --- BEACH/DESERT SAND OVERLAY CONFIG ---
            boolean enableBeachRedSand, // toggle the beach/desert red-sand overlay on patches
            Block beachRedSandBlock,    // which block to use when enabled (e.g., Blocks.RED_SAND)

            // Optional: if non-null, use this noise settings id as-is for the LevelStem
            ResourceLocation customNoiseSettingsId
    ) {}

    /* ----------------- PROFILES ----------------- */

    public static final Profile HARAMBE_DEFAULT = new Profile(
            "harambe_default",
            ModBlocks.CAROTENE_GRASS_BLOCK.get(),
            Blocks.PODZOL,
            ModBlocks.CHOCO_CREAM_STONE.get(),
            ModBlocks.CAROTENE_SHORT_GRASS.get(),
            1.00f, 1.00f,                       // size, coverage
            null, 0, 0,                         // no trees by default
            true, Blocks.RED_SAND,              // beach/desert overlay
            null                                // normal overworld with overlay composed in
    );

    public static final Profile BELNADES_DEFAULT = new Profile(
            "belnades_default",
            ModBlocks.BELMONT_GRASS_BLOCK.get(),
            ModBlocks.BELMONT_GRASS_BLOCK.get(),
            Blocks.DIRT,
            ModBlocks.BELMONT_SHORT_GRASS.get(),
            1.00f, 1.00f,
            TreeStyle.BELMONT, 21, 1,
            true, Blocks.LIME_CONCRETE_POWDER,
            ResourceLocation.fromNamespaceAndPath("harambefmod", "belnades_funkynaza")
    );

    public static final Profile DYNASTIRIUM_DEFAULT = new Profile(
            "dynastirium_default",
            ModBlocks.DYNASTY_GRASS_BLOCK.get(),
            ModBlocks.DYNASTY_GRASS_BLOCK.get(),
            Blocks.DIRT,
            ModBlocks.DYNASTY_SHORT_GRASS.get(),
            1.00f, 1.00f,
            TreeStyle.DYNASTY, 20, 1,
            true, Blocks.BLACK_CONCRETE_POWDER,
            null
    );

    public static final Profile IMPERO_DEFAULT = new Profile(
            "impero_default",
            ModBlocks.IMPERIUM_GRASS_BLOCK.get(),
            ModBlocks.IMPERIUM_GRASS_BLOCK.get(),
            Blocks.DIRT,
            ModBlocks.IMPERIUM_SHORT_GRASS.get(),
            1.00f, 1.00f,
            TreeStyle.IMPERIUM, 18, 1,
            true, Blocks.BLUE_CONCRETE_POWDER,
            // use our merged amplified-with-imperium overlay
            ResourceLocation.fromNamespaceAndPath("harambefmod", "settings_impero_overlay_merged")
    );

    public static final Profile MARCHELUS_DEFAULT = new Profile(
            "marchelus_default",
            ModBlocks.MISCHIEF_GRASS_BLOCK.get(),
            ModBlocks.MISCHIEF_GRASS_BLOCK.get(),
            Blocks.DIRT,
            ModBlocks.MISCHIEF_SHORT_GRASS.get(),
            1.00f, 1.00f,
            TreeStyle.MISCHIEF, 24, 1,
            true, Blocks.PURPLE_CONCRETE_POWDER,
            null
    );

    public static final Profile[] ALL = new Profile[] {
            HARAMBE_DEFAULT, BELNADES_DEFAULT, DYNASTIRIUM_DEFAULT, IMPERO_DEFAULT, MARCHELUS_DEFAULT
    };

    private PatchProfiles() {}
}
