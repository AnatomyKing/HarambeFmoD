package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
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

    /* ---- Ceiling-only survival & placement (accept egg OR sturdy ceiling) ---- */

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        BlockPos above = pos.above();
        BlockState ceiling = level.getBlockState(above);
        boolean sturdy = ceiling.isFaceSturdy(level, above, Direction.DOWN);
        boolean eggCeiling = ceiling.is(ModBlocks.BANANA_COW_EGG.get());
        return sturdy || eggCeiling;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        BlockState s = defaultBlockState();
        return canSurvive(s, ctx.getLevel(), pos) ? s : null;
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state,
                                           @NotNull Direction dir,
                                           @NotNull BlockState neighborState,
                                           @NotNull LevelAccessor level,
                                           @NotNull BlockPos pos,
                                           @NotNull BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState(); // pop off if unsupported
        }
        return super.updateShape(state, dir, neighborState, level, pos, neighborPos);
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level,
                                @NotNull BlockPos pos, @NotNull Block neighbor, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighbor, fromPos, isMoving);
        if (!level.isClientSide && !state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true); // drop when support is lost
        }
    }

    /* ---------------- Random “growth” into an egg ---------------- */

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state) { return true; }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level,
                           @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (random.nextInt(3) == 0) {
            tryConvertUnderOak(level, pos);
        }
    }

    /* ---------------- Bonemeal API ---------------- */

    @Override
    public boolean isValidBonemealTarget(@NotNull LevelReader level,
                                         @NotNull BlockPos pos,
                                         @NotNull BlockState state) {
        // Must be under oak and have air below to move the flower down on sprout
        return hasOakAbove(level, pos) && hasAirBelow(level, pos);
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
        tryConvertUnderOak(level, pos);
    }

    /* ---------------- Helpers ---------------- */

    private static boolean hasOakAbove(LevelReader level, BlockPos flowerPos) {
        return level.getBlockState(flowerPos.above()).is(BlockTags.OAK_LOGS);
    }

    private static boolean hasAirBelow(LevelReader level, BlockPos flowerPos) {
        return level.getBlockState(flowerPos.below()).isAir();
    }

    /**
     * Under OAK log and air below:
     *   pos.above()  = OAK_LOG
     *   pos          = BANANA_COW_EGG (attached=true, age=0)
     *   pos.below()  = MUSAVACCA_FLOWER
     */
    private void tryConvertUnderOak(ServerLevel level, BlockPos flowerPos) {
        if (!hasOakAbove(level, flowerPos) || !hasAirBelow(level, flowerPos)) return;

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

    /** If the flower is removed: hatch ripe egg above, else just remove it. */
    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                         @NotNull BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            BlockPos eggPos = pos.above();
            BlockState eggState = level.getBlockState(eggPos);
            if (eggState.getBlock() instanceof BananaCowEggBlock) {
                if (eggState.getValue(BananaCowEggBlock.AGE) == 2) {
                    BananaCowEggBlock.hatch((ServerLevel) level, eggPos, true);
                }
                level.removeBlock(eggPos, false);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
