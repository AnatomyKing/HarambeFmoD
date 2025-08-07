package net.anatomyworld.harambefmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.anatomyworld.harambefmod.block.entity.PearlFireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;

public final class PearlFireBlock extends BaseFireBlock implements EntityBlock {
    public static final MapCodec<PearlFireBlock> CODEC = BlockBehaviour.simpleCodec(PearlFireBlock::new);
    @Override protected @NotNull MapCodec<? extends BaseFireBlock> codec() { return CODEC; }

    public PearlFireBlock() {
        this(makeProps());
    }
    private PearlFireBlock(BlockBehaviour.Properties props) {
        super(props, 1.0F);
    }

    private static BlockBehaviour.Properties makeProps() {
        return BlockBehaviour.Properties.of()
                .noCollission()
                .noOcclusion()
                .replaceable()
                .instabreak()
                .dynamicShape()
                .randomTicks()
                .pushReaction(PushReaction.DESTROY)
                .noLootTable()
                .sound(SoundType.WOOL)
                .lightLevel(s -> 15);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        // Create a new PearlFireBlockEntity at this position
        return new PearlFireBlockEntity(pos, state);
    }

    @Override
    protected boolean canBurn(BlockState neighbour) {
        // Allow fire to burn if the neighboring block is flammable (checks UP direction by default)
        return neighbour.isFlammable(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO, net.minecraft.core.Direction.UP);
    }
}
