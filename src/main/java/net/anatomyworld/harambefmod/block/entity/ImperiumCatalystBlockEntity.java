package net.anatomyworld.harambefmod.block.entity;

import net.anatomyworld.harambefmod.block.ModBlockEntities;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.faction.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ImperiumCatalystBlockEntity extends FactionCatalystBlockEntity {
    public ImperiumCatalystBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IMPERIUM_CATALYST_ENTITY.get(), pos, state);
    }
    @Override public Faction faction() { return Faction.IMPERIUM; }
    @Override protected Block grassBlock() { return ModBlocks.IMPERIUM_GRASS_BLOCK.get(); }
    @Override protected Block veinBlock()  { return ModBlocks.IMPERIUM_VEIN.get(); }
}
