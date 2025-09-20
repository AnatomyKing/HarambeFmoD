package net.anatomyworld.harambefmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallBannerBlock extends DirectionalBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<WallBannerBlock> CODEC = Block.simpleCodec(WallBannerBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape NORTH_SHAPE = Block.box(5, 0, 8, 11, 16, 16); // centered pole N
    private static final VoxelShape SOUTH_SHAPE = Block.box(5, 0, 0, 11, 16, 8);  // centered pole S
    private static final VoxelShape WEST_SHAPE  = Block.box(8, 0, 5, 16, 16, 11); // centered pole W
    private static final VoxelShape EAST_SHAPE  = Block.box(0, 0, 5, 8, 16, 11);  // centered pole E

    public WallBannerBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() { return CODEC; }

    /** Wall-only survival check */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction face = state.getValue(FACING);

        // reject UP/DOWN explicitly
        if (!face.getAxis().isHorizontal()) {
            return false;
        }

        BlockPos behind = pos.relative(face.getOpposite());
        return level.getBlockState(behind).isFaceSturdy(level, behind, face);
    }

    /** Wall-only placement */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        var level = ctx.getLevel();
        var pos   = ctx.getClickedPos();
        var fluid = level.getFluidState(pos);

        Direction clicked = ctx.getClickedFace();
        if (!clicked.getAxis().isHorizontal()) return null; // disallow UP/DOWN

        BlockState s = defaultBlockState()
                .setValue(FACING, clicked)
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
        return s.canSurvive(level, pos) ? s : null;
    }

    // 1.21.8 signature
    @Override
    public BlockState updateShape(BlockState state,
                                  LevelReader level,
                                  ScheduledTickAccess ticks,
                                  BlockPos pos,
                                  Direction dir,
                                  BlockPos neighborPos,
                                  BlockState neighborState,
                                  RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (dir == state.getValue(FACING).getOpposite() && !this.canSurvive(state, level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public FluidState getFluidState(BlockState s) {
        return s.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(s);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST  -> WEST_SHAPE;
            case EAST  -> EAST_SHAPE;
            default    -> NORTH_SHAPE; // UP/DOWN will never happen
        };
    }
}
