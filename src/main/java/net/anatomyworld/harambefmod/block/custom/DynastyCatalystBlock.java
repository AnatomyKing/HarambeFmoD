package net.anatomyworld.harambefmod.block.custom;


import net.anatomyworld.harambefmod.block.entity.DynastyCatalystBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DynastyCatalystBlock extends FactionCatalystBlock {
    public DynastyCatalystBlock(Properties props) { super(props); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DynastyCatalystBlockEntity(pos, state);
    }
}
