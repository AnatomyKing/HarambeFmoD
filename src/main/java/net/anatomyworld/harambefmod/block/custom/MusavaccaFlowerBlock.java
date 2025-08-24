package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.data.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MusavaccaFlowerBlock extends Block implements BonemealableBlock {
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 14, 14);

    public MusavaccaFlowerBlock(BlockBehaviour.Properties props) {
        super(props.sound(SoundType.CROP)); // keep .randomTicks() in registry json if needed
    }

    // 1.21.x signature: BlockGetter (not LevelReader)
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state,
                                        @NotNull BlockGetter level,
                                        @NotNull BlockPos pos,
                                        @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    /* Ceiling-only survival: either a sturdy ceiling OR the egg (so it doesn't pop under an attached egg). */
    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        BlockPos above = pos.above();
        BlockState ceiling = level.getBlockState(above);
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
        // vanilla pop-off if ceiling is no longer valid
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        // !! use the new super signature (8 args), not the old 6-arg version
        return super.updateShape(state, level, scheduled, pos, dir, neighborPos, neighborState, random);
    }

    @Override public boolean isRandomlyTicking(@NotNull BlockState state) { return true; }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level,
                           @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (random.nextInt(3) == 0) {
            tryConvertUnderGrowthSurface(level, pos);
        }
    }

    /* -------- Bonemeal -------- */

    @Override
    public boolean isValidBonemealTarget(@NotNull LevelReader level,
                                         @NotNull BlockPos pos,
                                         @NotNull BlockState state) {
        return hasGrowthSurfaceAbove(level, pos) && hasAirBelow(level, pos);
    }

    @Override
    public boolean isBonemealSuccess(@NotNull Level level,
                                     @NotNull RandomSource random,
                                     @NotNull BlockPos pos,
                                     @NotNull BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(@NotNull ServerLevel level,
                                @NotNull RandomSource random,
                                @NotNull BlockPos pos,
                                @NotNull BlockState state) {
        tryConvertUnderGrowthSurface(level, pos);
    }

    /* -------- Helpers -------- */

    private static boolean hasGrowthSurfaceAbove(LevelReader level, BlockPos flowerPos) {
        return level.getBlockState(flowerPos.above()).is(ModTags.Blocks.BANANA_COW_GROWTH);
        // (tag contains your allowed top blocks)
    }

    private static boolean hasAirBelow(LevelReader level, BlockPos flowerPos) {
        return level.getBlockState(flowerPos.below()).isAir();
    }

    /**
     * If directly under any block in #harambefmod:banana_cow_growth and there is air below, convert into the 3-block
     * egg+flower stack:
     *   pos.above()  = growth surface (unchanged)
     *   pos          = BANANA_COW_EGG (attached=true, age=0)
     *   pos.below()  = MUSAVACCA_FLOWER
     */
    private void tryConvertUnderGrowthSurface(ServerLevel level, BlockPos flowerPos) {
        if (!hasGrowthSurfaceAbove(level, flowerPos) || !hasAirBelow(level, flowerPos)) return;

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

    /**
     * If THIS flower is removed:
     * - emulate egg "break" (FX + air),
     * - hatch if age=2.
     */
    @Override
    public void onBlockStateChange(@NotNull LevelReader level,
                                   @NotNull BlockPos pos,
                                   @NotNull BlockState oldState,
                                   @NotNull BlockState newState) {
        // Only react when THIS block was replaced by something else
        if (oldState.getBlock() == this && newState.getBlock() != this && level instanceof ServerLevel sl) {
            BlockPos eggPos = pos.above();
            BlockState eggState = sl.getBlockState(eggPos);

            if (eggState.getBlock() instanceof BananaCowEggBlock) {
                sl.levelEvent(2001, eggPos, Block.getId(eggState)); // break FX
                sl.setBlock(eggPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

                int age = eggState.getValue(BananaCowEggBlock.AGE);
                if (age == 2) {
                    BananaCowEggBlock.hatch(sl, eggPos, true);
                }
                sl.gameEvent(null, GameEvent.BLOCK_DESTROY, eggPos);
            }
        }
    }
}
