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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MusavaccaPlantSaplingBlock extends BushBlock implements BonemealableBlock {

    public MusavaccaPlantSaplingBlock(BlockBehaviour.Properties props) { super(props); }

    public static final MapCodec<MusavaccaPlantSaplingBlock> CODEC =
            BlockBehaviour.simpleCodec(MusavaccaPlantSaplingBlock::new);

    @Override
    public MapCodec<? extends BushBlock> codec() { return CODEC; }

    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos pos) {
        return ground.is(Blocks.FARMLAND) || ground.is(Blocks.DIRT) || ground.is(Blocks.GRASS_BLOCK);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
        if (level.getRawBrightness(pos, 0) >= 9 && rng.nextInt(7) == 0) {
            tryGrow(level, pos);
        }
    }

    @Override public boolean isValidBonemealTarget(LevelReader l, BlockPos p, BlockState s) { return true; }
    @Override public boolean isBonemealSuccess(Level l, RandomSource r, BlockPos p, BlockState s) { return true; }
    @Override public void performBonemeal(ServerLevel level, RandomSource rng, BlockPos pos, BlockState state) {
        tryGrow(level, pos);
    }

    private void tryGrow(ServerLevel level, BlockPos pos) {
        if (!canPlaceTree(level, pos)) return;
        MusavaccaTreePlacer.place(level, pos);
    }

    /* ======= space check for the exact 5×7×5 footprint ======= */

    private static boolean isSoft(BlockState s) {
        return s.isAir()
                || s.canBeReplaced()
                || s.is(BlockTags.LEAVES)
                || s.is(BlockTags.SAPLINGS)     // allow replacing the sapling itself
                || s.is(Blocks.SNOW)
                || s.is(Blocks.VINE)
                || s.is(Blocks.FERN)
                || s.is(Blocks.LARGE_FERN)
                || s.is(Blocks.TALL_GRASS);
    }

    private boolean canPlaceTree(ServerLevel level, BlockPos origin) {
        int topY = origin.getY() + 6;
        if (origin.getY() < level.getMinBuildHeight() || topY >= level.getMaxBuildHeight()) return false;

        var rel = (java.util.function.Function<int[], BlockPos>) xyz -> origin.offset(xyz[0] - 2, xyz[1], xyz[2] - 2);
        var ok  = (java.util.function.Predicate<int[]>) xyz -> isSoft(level.getBlockState(rel.apply(xyz)));

        for (int y = 0; y <= 5; y++) if (!ok.test(new int[]{2, y, 2})) return false;
        int[][] y2 = {{1,2,2},{2,2,1},{2,2,3},{3,2,2}};
        for (int[] p : y2) if (!ok.test(p)) return false;
        int[][] y3 = {{1,3,2},{3,3,2},{2,3,1},{2,3,3},{1,3,1},{1,3,3},{3,3,1},{3,3,3}};
        for (int[] p : y3) if (!ok.test(p)) return false;
        int[][] y4 = {{0,4,2},{4,4,2},{1,4,1},{1,4,3},{3,4,1},{3,4,3},{1,4,2},{3,4,2},{2,4,1},{2,4,3},{2,4,2}};
        for (int[] p : y4) if (!ok.test(p)) return false;
        int[][] y5 = {{1,5,2},{2,5,1},{2,5,3},{3,5,2},{2,5,2}};
        for (int[] p : y5) if (!ok.test(p)) return false;

        return ok.test(new int[]{2,6,2});
    }
}
