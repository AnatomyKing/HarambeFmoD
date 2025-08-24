package net.anatomyworld.harambefmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.anatomyworld.harambefmod.world.MusavaccaTreePlacer;
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

public class MusavaccaPlantSaplingBlock extends VegetationBlock implements BonemealableBlock {

    // Mojang pattern: subclass returns a codec of itself; method type is <? extends VegetationBlock>
    public static final MapCodec<MusavaccaPlantSaplingBlock> CODEC =
            BlockBehaviour.simpleCodec(MusavaccaPlantSaplingBlock::new);

    public MusavaccaPlantSaplingBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    // Same predicate you had, using VegetationBlock's hook
    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos pos) {
        return ground.is(Blocks.FARMLAND)
                || ground.is(Blocks.DIRT)
                || ground.is(Blocks.GRASS_BLOCK);
    }

    /* ---------- Random growth ---------- */

    // Keep if you register this block WITHOUT .randomTicks() on the Properties.
    // If you DO set .randomTicks() in the registry (as in your blocks class), you may omit this override.
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
        if (level.getRawBrightness(pos, 0) >= 9 && rng.nextInt(7) == 0) {
            tryGrow(level, pos);
        }
    }

    /* ---------- BonemealableBlock (1.21.8 signatures) ---------- */

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        tryGrow(level, pos);
    }

    /* ---------- Growth helpers ---------- */

    private void tryGrow(ServerLevel level, BlockPos pos) {
        if (!canPlaceTree(level, pos)) return;
        MusavaccaTreePlacer.place(level, pos);
    }

    // allow replacing leaves/vines/grass etc. as you had
    private static boolean isSoft(BlockState s) {
        return s.isAir()
                || s.canBeReplaced()
                || s.is(BlockTags.LEAVES)
                || s.is(BlockTags.SAPLINGS)
                || s.is(Blocks.SNOW)
                || s.is(Blocks.VINE)
                || s.is(Blocks.FERN)
                || s.is(Blocks.LARGE_FERN)
                || s.is(Blocks.TALL_GRASS);
    }

    /**
     * Exact 5×7×5 footprint space check centered on origin (with origin at footprint center X/Z, y at ground).
     * Updated to LevelHeightAccessor methods getMinY()/getMaxY() for 1.21.8.
     */
    private boolean canPlaceTree(ServerLevel level, BlockPos origin) {
        int topY = origin.getY() + 6;
        if (origin.getY() < level.getMinY() || topY >= level.getMaxY()) return false;

        java.util.function.Function<int[], BlockPos> rel = xyz -> origin.offset(xyz[0] - 2, xyz[1], xyz[2] - 2);
        java.util.function.Predicate<int[]> ok = xyz -> isSoft(level.getBlockState(rel.apply(xyz)));

        // y = 0..5 trunk column
        for (int y = 0; y <= 5; y++) if (!ok.test(new int[]{2, y, 2})) return false;

        int[][] y2 = {{1,2,2},{2,2,1},{2,2,3},{3,2,2}};
        for (int[] p : y2) if (!ok.test(p)) return false;

        int[][] y3 = {{1,3,2},{3,3,2},{2,3,1},{2,3,3},{1,3,1},{1,3,3},{3,3,1},{3,3,3}};
        for (int[] p : y3) if (!ok.test(p)) return false;

        int[][] y4 = {{0,4,2},{4,4,2},{2,4,0},{2,4,4},{1,4,1},{1,4,3},{3,4,1},{3,4,3},
                {1,4,2},{3,4,2},{2,4,1},{2,4,3},{2,4,2}};
        for (int[] p : y4) if (!ok.test(p)) return false;

        int[][] y5 = {{1,5,2},{2,5,1},{2,5,3},{3,5,2},{2,5,2}};
        for (int[] p : y5) if (!ok.test(p)) return false;

        return ok.test(new int[]{2,6,2});
    }
}
