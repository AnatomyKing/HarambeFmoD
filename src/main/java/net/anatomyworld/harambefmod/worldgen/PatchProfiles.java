package net.anatomyworld.harambefmod.worldgen;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * One profile = one dimension's "patch recipe".
 * - coreTop: inner patch block
 * - edgeTop: outer/dither ring block
 * - underBlock: 1-layer under both core/edge
 * - shortGrassSwap: replaces vanilla SHORT_GRASS on those surfaces
 * - large/amplified: passed to NoiseGeneratorSettings.overworld(ctx, large, amplified)
 */
public final class PatchProfiles {

    public record Profile(
            String suffix,
            Block coreTop,
            Block edgeTop,
            Block underBlock,
            Block shortGrassSwap,
            boolean largeOverworld,
            boolean amplified
    ) {}

    /** Example for your first dimension ("overworldnew"). */
    public static final Profile OVERWORLDNEW = new Profile(
            "overworldnew",
            ModBlocks.CAROTENE_GRASS_BLOCK.get(), // core top
            Blocks.PODZOL,                        // edge (ring + dither)
            ModBlocks.CHOCO_CREAM_STONE.get(),    // underlay
            ModBlocks.CAROTENE_SHORT_GRASS.get(), // short-grass swap
            false,                                // largeOverworld (false = normal)
            false                                 // amplified (true for amplified terrain)
    );

    // TODO: Add up to 3 more profiles for your other dimensions:
    // public static final Profile DIM2 = new Profile(...);
    // public static final Profile DIM3 = new Profile(...);
    // public static final Profile DIM4 = new Profile(...);

    public static final Profile[] ALL = new Profile[] {
            OVERWORLDNEW
            // , DIM2, DIM3, DIM4
    };

    private PatchProfiles() {}
}
