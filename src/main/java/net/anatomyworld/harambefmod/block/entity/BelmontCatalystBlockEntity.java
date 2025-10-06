package net.anatomyworld.harambefmod.block.entity;

import net.anatomyworld.harambefmod.block.ModBlockEntities;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.faction.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BelmontCatalystBlockEntity extends FactionCatalystBlockEntity {
    public BelmontCatalystBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BELMONT_CATALYST_ENTITY.get(), pos, state);
    }
    @Override public Faction faction() { return Faction.BELMONT; }
    @Override protected Block grassBlock() { return ModBlocks.BELMONT_GRASS_BLOCK.get(); }
    @Override protected Block veinBlock()  { return ModBlocks.BELMONT_VEIN.get(); }
}
