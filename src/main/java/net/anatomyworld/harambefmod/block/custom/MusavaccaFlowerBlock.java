package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.data.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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
        super(props.sound(SoundType.CROP)); // keep .randomTicks() in registry
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state,
                                        @NotNull net.minecraft.world.level.BlockGetter level,
                                        @NotNull BlockPos pos,
                                        @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    /* Ceiling-only survival: either a sturdy ceiling OR the egg (so the flower under an attached egg doesn't instantly pop). */
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

    /* Vanilla pop-off: returning AIR here triggers onRemove. */
    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state,
                                           @NotNull Direction dir,
                                           @NotNull BlockState neighborState,
                                           @NotNull LevelAccessor level,
                                           @NotNull BlockPos pos,
                                           @NotNull BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, dir, neighborState, level, pos, neighborPos);
    }

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state) { return true; }

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
    }

    private static boolean hasAirBelow(LevelReader level, BlockPos flowerPos) {
        return level.getBlockState(flowerPos.below()).isAir();
    }

    /**
     * If directly under any block in #harambefmod:banana_cow_growth and there is air below:
     *   pos.above()  = growth surface
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
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                         @NotNull BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            BlockPos eggPos = pos.above();
            BlockState eggState = level.getBlockState(eggPos);

            if (eggState.getBlock() instanceof BananaCowEggBlock) {
                // break FX (particles+sound)
                level.levelEvent(2001, eggPos, Block.getId(eggState)); // vanilla destroy effect
                // free the space like a real break
                level.setBlock(eggPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

                int age = eggState.getValue(BananaCowEggBlock.AGE);
                if (age == 2 && level instanceof ServerLevel sl) {
                    BananaCowEggBlock.hatch(sl, eggPos, true); // attached-style spawn
                }
                level.gameEvent(null, GameEvent.BLOCK_DESTROY, eggPos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
