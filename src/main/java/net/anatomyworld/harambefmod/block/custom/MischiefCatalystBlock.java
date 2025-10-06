package net.anatomyworld.harambefmod.block.custom;


import net.anatomyworld.harambefmod.block.entity.MischiefCatalystBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MischiefCatalystBlock extends FactionCatalystBlock {
    public MischiefCatalystBlock(BlockBehaviour.Properties props) { super(props); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MischiefCatalystBlockEntity(pos, state);
    }
}
