package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.entity.FactionCatalystBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public abstract class FactionCatalystBlock extends SculkCatalystBlock implements EntityBlock {
    protected FactionCatalystBlock(Properties props) { super(props); }

    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return level.isClientSide ? null
                : (lvl, p, st, be) -> {
            if (be instanceof FactionCatalystBlockEntity f) {
                FactionCatalystBlockEntity.serverTick(lvl, p, st, f);
            }
        };
    }
}
