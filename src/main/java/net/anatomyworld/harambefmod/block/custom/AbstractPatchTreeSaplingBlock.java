package net.anatomyworld.harambefmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.anatomyworld.harambefmod.worldgen.TreeStyles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Vanilla-style base for "tree-builder" saplings.
 * - Random tick growth + bonemeal pacing like vanilla saplings
 * - Two-stage system (stage: 0 -> 1 -> tree)
 * - Lightweight clearance check (soft blocks are allowed)
 *
 * Subclasses only need to provide:
 *   - builder()
 *   - estMaxHeight()
 *   - estNearRadius()
 *   - codec()
 */
public abstract class AbstractPatchTreeSaplingBlock extends VegetationBlock implements BonemealableBlock {

    /** Matches vanilla's 2-stage sapling approach. */
    public static final IntegerProperty STAGE = BlockStateProperties.STAGE; // 0..1

    protected AbstractPatchTreeSaplingBlock(BlockBehaviour.Properties props) {
        super(props);
        // default: stage 0
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    /** Subclasses provide the tree generator. */
    protected abstract TreeStyles.PatchTreeBuilder builder();

    /** Coarse upper bound used only for a quick vertical check. */
    protected abstract int estMaxHeight();

    /** Small near-trunk radius to nudge obvious obstructions (not the full canopy size). */
    protected abstract int estNearRadius();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STAGE);
    }

    // ---------- Vanilla-ish placement rules ----------
    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos pos) {
        // Vanilla saplings use the dirt tag. Covers dirt, grass, podzol, coarse dirt, farmland, moss, etc.
        return ground.is(BlockTags.DIRT);
    }

    // If you also set .randomTicks() in registration, keep this true so Minecraft calls randomTick.
    @Override public boolean isRandomlyTicking(BlockState state) { return true; }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
        // Vanilla pacing: needs light and a luck roll
        if (level.getRawBrightness(pos.above(), 0) >= 9 && rng.nextInt(7) == 0) {
            int stage = state.getValue(STAGE);
            if (stage == 0) {
                // advance to stage 1 (no tree yet)
                level.setBlock(pos, state.setValue(STAGE, 1), 4);
            } else {
                // at stage 1 → attempt to grow the full tree
                tryGrow(level, pos);
            }
        }
    }

    // ---------- BonemealableBlock ----------
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return true; }

    // Return true so bonemeal is consumed like vanilla; success/advance is decided in performBonemeal().
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        // Vanilla-like feel: bonemeal often needs a couple of clicks.
        // Use a moderate chance to advance; otherwise, bonemeal is consumed with no visible result.
        if (random.nextFloat() < 0.45f) { // ≈ vanilla feel; tweak if desired
            int stage = state.getValue(STAGE);
            if (stage == 0) {
                level.setBlock(pos, state.setValue(STAGE, 1), 4);
            } else {
                tryGrow(level, pos);
            }
        }
    }

    // ---------- Growth ----------
    protected void tryGrow(ServerLevel level, BlockPos pos) {
        if (!canPlaceTree(level, pos)) return;

        // Remove, attempt, restore on failure (vanilla sapling pattern)
        BlockState sapling = level.getBlockState(pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 4);

        boolean success = false;
        try {
            success = builder().place(level, pos, level.getRandom());
        } finally {
            if (!success) {
                level.setBlock(pos, sapling, 4);
            }
        }
    }

    // Allow replacing air/leaves/grass/etc. so canopy can merge into soft blocks.
    private static boolean isSoft(BlockState s) {
        return s.isAir()
                || s.canBeReplaced()
                || s.is(BlockTags.LEAVES)
                || s.is(BlockTags.SAPLINGS)
                || s.is(Blocks.SNOW)
                || s.is(Blocks.VINE)
                || s.is(Blocks.FERN)
                || s.is(Blocks.LARGE_FERN)
                || s.is(Blocks.TALL_GRASS)
                || s.is(Blocks.MOSS_CARPET)
                || s.is(Blocks.MOSS_BLOCK);
    }

    /**
     * Lightweight clearance probe:
     *  - Ensures a clear trunk column up to ~8 blocks or the tree's estimated height, whichever is smaller.
     *  - Checks a small plus (+) around the trunk near the ground so arms/roots don’t collide immediately.
     *  - Lets leaves/vines/grass stay, because the builders replace them safely.
     */
    protected boolean canPlaceTree(ServerLevel level, BlockPos origin) {
        int maxH = Math.max(6, estMaxHeight());
        int topY = origin.getY() + Math.min(8, maxH) + 1;

        if (origin.getY() < level.getMinY() || topY >= level.getMaxY()) return false;

        // trunk column check
        for (int y = 0; y <= Math.min(8, maxH); y++) {
            BlockPos p = origin.above(y);
            if (!isSoft(level.getBlockState(p))) return false;
        }

        // small near-trunk ring at y=1..3
        int r = Math.max(1, Math.min(3, estNearRadius()));
        for (int y = 1; y <= 3; y++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) != 1) continue; // plus, not full disk
                    BlockPos p = origin.offset(dx, y, dz);
                    if (!isSoft(level.getBlockState(p))) return false;
                }
            }
        }
        return true;
    }

    // VegetationBlock’s codec() must be implemented by each concrete subclass;
    // the abstract base doesn’t declare a codec.
}
