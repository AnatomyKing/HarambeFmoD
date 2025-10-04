// MischiefCatalystBlock.java
package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.block.ModBlockEntities;
import net.anatomyworld.harambefmod.block.entity.MischiefCatalystBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MischiefCatalystBlock extends SculkCatalystBlock implements EntityBlock {
    public MischiefCatalystBlock(Properties props) { super(props); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MischiefCatalystBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.MISCHIEF_CATALYST_ENTITY.get(),
                (lvl, p, st, be) -> MischiefCatalystBlockEntity.serverTick(lvl, p, st, (MischiefCatalystBlockEntity) be));
    }
}
