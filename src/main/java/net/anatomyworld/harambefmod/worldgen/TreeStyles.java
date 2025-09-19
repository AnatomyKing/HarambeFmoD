package net.anatomyworld.harambefmod.worldgen;

import net.anatomyworld.harambefmod.worldgen.trees.BelmontTreeBuilder;
import net.anatomyworld.harambefmod.worldgen.trees.DynastyTreeBuilder;
import net.anatomyworld.harambefmod.worldgen.trees.ImperiumTreeBuilder;
import net.anatomyworld.harambefmod.worldgen.trees.MischiefTreeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

import static net.anatomyworld.harambefmod.worldgen.PatchProfiles.TreeStyle;

/**
 * Entry point & small shared helpers for custom tree builders.
 *
 * Builders are split into separate classes:
 * - BelmontTreeBuilder
 * - DynastyTreeBuilder
 */
public final class TreeStyles {
    private TreeStyles() {}

    /** Register all builders here so your feature code can look them up by style. */
    public static final Map<TreeStyle, PatchTreeBuilder> BY_STYLE = Map.of(
            TreeStyle.BELMONT, new BelmontTreeBuilder(),
            TreeStyle.DYNASTY, new DynastyTreeBuilder(),
            TreeStyle.IMPERIUM, new ImperiumTreeBuilder(),
            TreeStyle.MISCHIEF, new MischiefTreeBuilder()
    );

    /* ---------- Shared helpers (call from builder classes) ---------- */

    /** Whether we can replace the current block when generating wood/leaves. */
    public static boolean canReplace(LevelAccessor level, BlockPos pos) {
        BlockState st = level.getBlockState(pos);
        if (st.isAir()) return true;
        if (st.is(Blocks.WATER) || st.is(Blocks.LAVA)) return false;
        // Vanilla tags: REPLACEABLE (plants, tall grass…), SNOW; allow empty-collision things.
        return st.is(BlockTags.REPLACEABLE) || st.is(net.minecraft.tags.BlockTags.SNOW)
                || (st.getFluidState().isEmpty() && st.getCollisionShape(level, pos).isEmpty());
    }

    /** Convenience for placing any pillar log with a given axis if replaceable. */
    public static void placeLogIfReplaceable(WorldGenLevel level, BlockState logState, BlockPos pos, net.minecraft.core.Direction.Axis axis) {
        if (TreeStyles.canReplace(level, pos)) {
            level.setBlock(pos, logState.setValue(RotatedPillarBlock.AXIS, axis), 2);
        }
    }

    /** Convenience for placing any leaves (PERSISTENT) if replaceable. */
    public static void placeLeavesIfReplaceable(WorldGenLevel level, BlockState leavesState, BlockPos pos) {
        if (TreeStyles.canReplace(level, pos)) {
            level.setBlock(pos, leavesState.setValue(LeavesBlock.PERSISTENT, true), 2);
        }
    }

    /* ---------- API ---------- */

    public interface PatchTreeBuilder {
        /** Place a tree at {@code origin}. Return true if something was placed. */
        boolean place(WorldGenLevel level, BlockPos origin, net.minecraft.util.RandomSource rand);
        String name();
    }
}
