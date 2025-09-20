package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.data.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MusavaccaFlowerBlock extends Block implements BonemealableBlock {
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 14, 14);

    public MusavaccaFlowerBlock(BlockBehaviour.Properties props) {
        super(props.sound(SoundType.CROP)); // keep .randomTicks() in registry if desired
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state,
                                        @NotNull BlockGetter level,
                                        @NotNull BlockPos pos,
                                        @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    /** Treat MOVING_PISTON/PISTON_HEAD above as valid support so the flower doesn’t pop while the stack is being pushed. */
    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        BlockPos above = pos.above();
        BlockState ceiling = level.getBlockState(above);

        if (ceiling.is(Blocks.MOVING_PISTON) || ceiling.is(Blocks.PISTON_HEAD)) {
            return true;
        }
        return ceiling.isFaceSturdy(level, above, Direction.DOWN)
                || ceiling.is(ModBlocks.BANANA_COW_EGG.get());
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        BlockState s = defaultBlockState();
        return canSurvive(s, ctx.getLevel(), pos) ? s : null;
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state,
                                              @NotNull LevelReader level,
                                              @NotNull ScheduledTickAccess scheduled,
                                              @NotNull BlockPos pos,
                                              @NotNull Direction dir,
                                              @NotNull BlockPos neighborPos,
                                              @NotNull BlockState neighborState,
                                              @NotNull RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, scheduled, pos, dir, neighborPos, neighborState, random);
    }

    @Override public boolean isRandomlyTicking(@NotNull BlockState state) { return true; }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level,
                           @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (isPistonAround(level, pos)) return; // avoid mid-move conversions
        if (random.nextInt(3) == 0) {
            tryConvertUnderGrowthSurface(level, pos);
        }
    }

    /* -------- Bonemeal -------- */
    @Override
    public boolean isValidBonemealTarget(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state) {
        return hasGrowthSurfaceAbove(level, pos) && hasAirBelow(level, pos);
    }
    @Override public boolean isBonemealSuccess(@NotNull Level level, @NotNull RandomSource random, @NotNull BlockPos pos, @NotNull BlockState state) { return true; }
    @Override public void performBonemeal(@NotNull ServerLevel level, @NotNull RandomSource random, @NotNull BlockPos pos, @NotNull BlockState state) {
        if (isPistonAround(level, pos)) return;
        tryConvertUnderGrowthSurface(level, pos);
    }

    /* -------- Helpers -------- */
    private static boolean hasGrowthSurfaceAbove(LevelReader level, BlockPos flowerPos) {
        return level.getBlockState(flowerPos.above()).is(ModTags.Blocks.BANANA_COW_GROWTH);
    }
    private static boolean hasAirBelow(LevelReader level, BlockPos flowerPos) {
        return level.getBlockState(flowerPos.below()).isAir();
    }
    private static boolean isPistonAround(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.MOVING_PISTON)
                || level.getBlockState(pos.above()).is(Blocks.MOVING_PISTON)
                || level.getBlockState(pos.below()).is(Blocks.MOVING_PISTON)
                || level.getBlockState(pos.above()).is(Blocks.PISTON_HEAD)
                || level.getBlockState(pos.below()).is(Blocks.PISTON_HEAD);
    }

    /** Convert this flower into an attached egg + place a new flower below. */
    private void tryConvertUnderGrowthSurface(ServerLevel level, BlockPos flowerPos) {
        if (!hasGrowthSurfaceAbove(level, flowerPos) || !hasAirBelow(level, flowerPos)) return;
        if (isPistonAround(level, flowerPos)) return;

        level.setBlock(
                flowerPos,
                ModBlocks.BANANA_COW_EGG.get().defaultBlockState()
                        .setValue(BananaCowEggBlock.AGE, 0)
                        .setValue(BananaCowEggBlock.ATTACHED, true),
                Block.UPDATE_ALL
        );

        level.setBlock(
                flowerPos.below(),
                ModBlocks.MUSAVACCA_FLOWER.get().defaultBlockState(),
                Block.UPDATE_ALL
        );
    }
}
