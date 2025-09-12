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
    public CaroteneGrassBlock(Properties props) { super(props.randomTicks()); }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
        BlockPos abovePos = pos.above();
        BlockState above = level.getBlockState(abovePos);

        // 1.21+: isSolidRender() has no parameters
        boolean covered = above.isSolidRender() || above.canOcclude();
        int light = level.getRawBrightness(abovePos, 0);

        if (covered || light < 4) {
            level.setBlock(pos, Blocks.ROOTED_DIRT.defaultBlockState(), 3);
        }
    }
}
