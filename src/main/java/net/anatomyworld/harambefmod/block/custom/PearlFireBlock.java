package net.anatomyworld.harambefmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.anatomyworld.harambefmod.block.entity.PearlFireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public final class PearlFireBlock extends BaseFireBlock implements EntityBlock {
    public static final MapCodec<PearlFireBlock> CODEC = BlockBehaviour.simpleCodec(PearlFireBlock::new);
    @Override protected @NotNull MapCodec<? extends BaseFireBlock> codec() { return CODEC; }

    public PearlFireBlock(BlockBehaviour.Properties props) {
        super(props, 1.0F); // additional props are applied in ModBlocks when registering
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PearlFireBlockEntity(pos, state);
    }

    @Override
    protected boolean canBurn(BlockState neighbour) {
        return neighbour.isFlammable(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.UP);
    }
}
