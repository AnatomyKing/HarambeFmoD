// NyliumGrassBlock.java — survives under snow, NO auto-spread (NeoForge 1.21.8 / Mojmap)
package net.anatomyworld.harambefmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;

public class NyliumGrassBlock extends SpreadingSnowyDirtBlock {
    public static final MapCodec<NyliumGrassBlock> CODEC = simpleCodec(NyliumGrassBlock::new);

    public NyliumGrassBlock(Properties props) {
        // keep random ticks so survival logic runs
        super(props.randomTicks());
    }

    @Override
    public MapCodec<NyliumGrassBlock> codec() {
        return CODEC;
    }

    /** Ensure correct initial snowy flag on placement. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        return defaultBlockState().setValue(SNOWY, isSnowySetting(level.getBlockState(pos.above())));
    }

    /** Disable spreading; only do survival + snowy flag maintenance. */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
        if (!canSurviveHere(level, pos)) {
            // swap to your base block if not plain dirt
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
            return;
        }

        // keep SNOWY property in sync with block above
        boolean snowy = isSnowySetting(level.getBlockState(pos.above()));
        if (state.getValue(SNOWY) != snowy) {
            level.setBlock(pos, state.setValue(SNOWY, snowy), 2);
        }

        // Intentionally skip the spread logic from SpreadingSnowyDirtBlock.
    }

    /** Vanilla-like survival: allow 1-layer snow; otherwise need light >= 4 and no solid occluder above. */
    private static boolean canSurviveHere(LevelReader level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState above = level.getBlockState(abovePos);

        // Single snow layer is explicitly allowed by vanilla grass survival logic.
        if (above.getBlock() instanceof SnowLayerBlock
                && above.hasProperty(SnowLayerBlock.LAYERS)
                && above.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }

        int light = level.getRawBrightness(abovePos, 0);
        boolean occludes = above.isSolidRender();
        return light >= 4 && !occludes;
    }
}
