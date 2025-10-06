package net.anatomyworld.harambefmod.block.entity;

import net.anatomyworld.harambefmod.block.ModBlockEntities;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.faction.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DynastyCatalystBlockEntity extends FactionCatalystBlockEntity {
    public DynastyCatalystBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DYNASTY_CATALYST_ENTITY.get(), pos, state);
    }
    @Override public Faction faction() { return Faction.DYNASTY; }
    @Override protected Block grassBlock() { return ModBlocks.DYNASTY_GRASS_BLOCK.get(); }
    @Override protected Block veinBlock()  { return ModBlocks.DYNASTY_VEIN.get(); }
}
