package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Directional, waterloggable crystal piece (small/medium/large/cluster).
 * Only survives when attached UP or DOWN to a HoneyCrystalBlock (the budding source).
 * Uses AmethystClusterBlock's own FACING/WATERLOGGED properties.
 */
public class HoneyCrystalClusterLikeBlock extends AmethystClusterBlock {

    public HoneyCrystalClusterLikeBlock(int height, int width, BlockBehaviour.Properties props) {
        super(height, width, props);
    }

    // NOTE: 1.21.x uses LevelReader here (not BlockGetter)
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        if (facing.getAxis() != Direction.Axis.Y) return false; // UP/DOWN only
        BlockPos supportPos = pos.relative(facing.getOpposite());
        BlockState support = level.getBlockState(supportPos);
        return support.is(ModBlocks.HONEY_CRYSTAL_BLOCK.get());
    }

    // 1.21.x neighbor update signature:
    // BlockState updateShape(BlockState, LevelReader, ScheduledTickAccess, BlockPos, Direction, BlockPos, BlockState, RandomSource)
    @Override
    protected BlockState updateShape(BlockState state,
                                     LevelReader level,
                                     ScheduledTickAccess scheduled,
                                     BlockPos pos,
                                     Direction fromDir,
                                     BlockPos fromPos,
                                     BlockState fromState,
                                     RandomSource random) {
        if (!this.canSurvive(state, level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, scheduled, pos, fromDir, fromPos, fromState, random);
    }
}
