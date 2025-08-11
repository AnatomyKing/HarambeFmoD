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
        super(props.sound(SoundType.CROP));
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state,
                                        @NotNull net.minecraft.world.level.BlockGetter level,
                                        @NotNull BlockPos pos,
                                        @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level,
                        @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) level.scheduleTick(pos, this, 1);
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level,
                                @NotNull BlockPos pos, @NotNull Block neighbor, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighbor, fromPos, isMoving);
        if (!level.isClientSide) level.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level,
                     @NotNull BlockPos pos, @NotNull RandomSource random) {
        trySproutEgg(level, pos);
    }

    private void trySproutEgg(ServerLevel level, BlockPos flowerPos) {
        BlockPos eggPos = flowerPos.above();       // egg grows above the flower
        BlockPos logPos = eggPos.above();          // oak log ceiling

        if (!level.getBlockState(eggPos).isAir()) return;

        // Only underneath OAK logs (log/wood/stripped variants are covered by the tag)
        if (level.getBlockState(logPos).is(BlockTags.OAK_LOGS)) {
            level.setBlock(eggPos, ModBlocks.BANANA_COW_EGG.get()
                            .defaultBlockState()
                            .setValue(BananaCowEggBlock.AGE, 0)
                            .setValue(BananaCowEggBlock.ATTACHED, true),
                    Block.UPDATE_ALL);
        }
    }

    /** If the flower is removed, also remove the egg above (egg controls its own drops). */
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
