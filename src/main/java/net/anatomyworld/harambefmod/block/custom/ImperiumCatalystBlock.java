package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.entity.ImperiumCatalystBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ImperiumCatalystBlock extends FactionCatalystBlock {
    public ImperiumCatalystBlock(Properties props) { super(props); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ImperiumCatalystBlockEntity(pos, state);
    }
}
