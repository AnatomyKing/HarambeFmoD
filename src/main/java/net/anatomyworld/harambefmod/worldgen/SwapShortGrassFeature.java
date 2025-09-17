package net.anatomyworld.harambefmod.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Converts vanilla SHORT_GRASS to each profile’s short-grass block
 * when the grass sits on top of that profile’s patch surface (core top or edge top).
 */
public final class SwapShortGrassFeature extends Feature<NoneFeatureConfiguration> {
    public static final SwapShortGrassFeature INSTANCE = new SwapShortGrassFeature();

    private SwapShortGrassFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();

        // The placed feature calls us multiple times in a chunk (CountPlacement/InSquare).
        // Each call, try a small scatter around the origin.
        for (int i = 0; i < 8; i++) {
            BlockPos pos = ctx.origin().offset(ctx.random().nextInt(8) - 4, 0, ctx.random().nextInt(8) - 4);
            pos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, pos);

            if (!level.getBlockState(pos).is(Blocks.SHORT_GRASS)) continue;

            BlockPos below = pos.below();
            var belowState = level.getBlockState(below);

            for (var p : net.anatomyworld.harambefmod.worldgen.PatchProfiles.ALL) {
                if (belowState.is(p.coreTop()) || belowState.is(p.edgeTop())) {
                    level.setBlock(pos, p.shortGrassSwap().defaultBlockState(), 2);
                    break;
                }
            }
        }
        return true;
    }
}
