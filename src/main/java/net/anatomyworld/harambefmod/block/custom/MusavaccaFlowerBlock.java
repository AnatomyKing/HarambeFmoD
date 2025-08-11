package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class MusavaccaFlowerBlock extends Block {
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 14, 14);

    public MusavaccaFlowerBlock(BlockBehaviour.Properties props) {
        super(props.sound(SoundType.CROP).randomTicks());
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state,
                                        @NotNull net.minecraft.world.level.BlockGetter level,
                                        @NotNull BlockPos pos,
                                        @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    /* Try immediately, then retry on changes/ticks */
    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level,
                        @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            if (!tryConvertUnderOak((ServerLevel) level, pos)) {
                level.scheduleTick(pos, this, 1);
            }
        }
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level,
                                @NotNull BlockPos pos, @NotNull Block neighbor,
                                @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighbor, fromPos, isMoving);
        if (!level.isClientSide) level.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level,
                     @NotNull BlockPos pos, @NotNull RandomSource random) {
        tryConvertUnderOak(level, pos);
    }

    /**
     * If placed directly under an OAK log and there is free space below,
     * transform the current flower block into the ATTACHED age-0 egg,
     * and move the flower one block DOWN.
     *
     * Layout after conversion:
     *   pos.above()  = OAK_LOG
     *   pos          = BANANA_COW_EGG (attached=true, age=0)
     *   pos.below()  = MUSAVACCA_FLOWER
     */
    private boolean tryConvertUnderOak(ServerLevel level, BlockPos flowerPos) {
        BlockPos logPos   = flowerPos.above();
        BlockPos belowPos = flowerPos.below();

        // Require oak log above and empty space below to move the flower down
        if (!level.getBlockState(logPos).is(BlockTags.OAK_LOGS)) return false;
        if (!level.getBlockState(belowPos).isAir()) return false;

        // Convert: put egg where the flower is…
        level.setBlock(flowerPos,
                ModBlocks.BANANA_COW_EGG.get().defaultBlockState()
                        .setValue(BananaCowEggBlock.AGE, 0)
                        .setValue(BananaCowEggBlock.ATTACHED, true),
                Block.UPDATE_ALL);

        // …and move the flower one block down.
        level.setBlock(belowPos,
                ModBlocks.MUSAVACCA_FLOWER.get().defaultBlockState(),
                Block.UPDATE_ALL);

        return true;
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
