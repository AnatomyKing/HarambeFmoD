// CaroteneGrassBlock.java
package net.anatomyworld.harambefmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FactionGrassBlock extends Block {
    public FactionGrassBlock(Properties props) { super(props.randomTicks()); }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
        BlockPos abovePos = pos.above();
        BlockState above = level.getBlockState(abovePos);

        // 1.21+: isSolidRender() has no parameters
        boolean covered = above.isSolidRender() || above.canOcclude();
        int light = level.getRawBrightness(abovePos, 0);

        if (covered || light < 4) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
        }
    }
}
