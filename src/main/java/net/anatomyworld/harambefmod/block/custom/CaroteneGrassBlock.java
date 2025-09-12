// CaroteneGrassBlock.java
package net.anatomyworld.harambefmod.block.custom;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;

public class CaroteneGrassBlock extends Block {
    public CaroteneGrassBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    // --- Nylium-style conversion: bonemeal Rooted Dirt adjacent to Carotene -> convert
    @SubscribeEvent
    public static void onBonemeal(BonemealEvent event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        if (!state.is(Blocks.ROOTED_DIRT)) return;
        if (!level.getBlockState(pos.above()).isAir()) return;

        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(d)).is(ModBlocks.CAROTENE_GRASS_BLOCK.get())) {
                if (!level.isClientSide() && level instanceof ServerLevel sl) {
                    sl.setBlock(pos, ModBlocks.CAROTENE_GRASS_BLOCK.get().defaultBlockState(), 3);
                }
                event.setSuccessful(true);
                event.setCanceled(true);
                return;
            }
        }
    }

    // --- “Dies” when covered/too dark, like grass/mycelium -> revert to ROOTED_DIRT
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
        BlockPos abovePos = pos.above();
        BlockState above = level.getBlockState(abovePos);

        boolean opaqueAbove = above.isSolidRender() || above.canOcclude();
        int lightAbove = level.getRawBrightness(abovePos, 0);

        // Matches vanilla behavior: covered OR very low light causes decay.
        // (Grass/mycelium need light; nylium decays with opaque above.)
        if (opaqueAbove || lightAbove < 4) { // 4 is the grass/mycelium death threshold
            level.setBlock(pos, Blocks.ROOTED_DIRT.defaultBlockState(), 3);
        }
    }
}
