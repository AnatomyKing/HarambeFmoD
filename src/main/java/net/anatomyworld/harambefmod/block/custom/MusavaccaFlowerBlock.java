package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class MusavaccaFlowerBlock extends Block implements BonemealableBlock {
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 14, 14);

    public MusavaccaFlowerBlock(BlockBehaviour.Properties props) {
        // keep .randomTicks() in your registry so randomTick runs
        super(props.sound(SoundType.CROP));
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state,
                                        @NotNull net.minecraft.world.level.BlockGetter level,
                                        @NotNull BlockPos pos,
                                        @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    /* ---------------- Random “growth” into an egg ---------------- */

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state) {
        return true; // we check conditions inside
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level,
                           @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (random.nextInt(3) == 0) { // ~1/3 chance to try
            tryConvertUnderOak(level, pos);
        }
    }

    /* ---------------- Bonemeal API ---------------- */

    @Override
    public boolean isValidBonemealTarget(@NotNull LevelReader level,
                                         @NotNull BlockPos pos,
                                         @NotNull BlockState state) {
        // must be directly under oak log AND have air below to move the flower down
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

    // Accept LevelReader here so you can call them from both bonemeal (LevelReader) and ticks.
    private static boolean hasOakAbove(LevelReader level, BlockPos flowerPos) {
        return level.getBlockState(flowerPos.above()).is(BlockTags.OAK_LOGS);
    }

    private static boolean hasAirBelow(LevelReader level, BlockPos flowerPos) {
        return level.getBlockState(flowerPos.below()).isAir();
    }

    /**
     * If directly under OAK log and there is air below:
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

    /** If the flower is removed later, also remove any egg above (no drops here). */
    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                         @NotNull BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            BlockPos eggPos = pos.above();
            if (level.getBlockState(eggPos).getBlock() instanceof BananaCowEggBlock) {
                level.destroyBlock(eggPos, false);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
