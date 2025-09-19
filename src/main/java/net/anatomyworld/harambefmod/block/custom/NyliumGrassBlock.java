// NyliumGrassBlock.java (NeoForge 1.21.8)
package net.anatomyworld.harambefmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;

public class NyliumGrassBlock extends SnowyDirtBlock {

    public NyliumGrassBlock(Properties props) {
        // Random ticks so it can decay like your example
        super(props.randomTicks());
    }

    /**
     * Ensure correct initial snowy state on placement (matches vanilla behavior).
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState above = level.getBlockState(pos.above());
        // SnowyDirtBlock exposes protected helper used by Grass/Dirt variants
        return this.defaultBlockState().setValue(SNOWY, isSnowySetting(above));
    }

    /**
     * Your decay logic (adapt as you like).
     * If covered or too dark, turn into dirt. Keep your earlier checks/signatures.
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
        BlockPos abovePos = pos.above();
        BlockState above = level.getBlockState(abovePos);

        // 1.21+: use the parameterless versions you used earlier
        boolean covered = above.isSolidRender() || above.canOcclude();
        int light = level.getRawBrightness(abovePos, 0);

        if (covered || light < 4) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
        }
        // Otherwise: keep state; SnowyDirtBlock will auto-toggle SNOWY based on the block above.
    }
}
