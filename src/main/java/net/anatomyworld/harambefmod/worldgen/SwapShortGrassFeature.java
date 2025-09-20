package net.anatomyworld.harambefmod.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Randomly swap SHORT_GRASS → profile short-grass variant, but only on your patch tops. */
public final class SwapShortGrassFeature extends Feature<NoneFeatureConfiguration> {
    public SwapShortGrassFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        for (int i = 0; i < 64; i++) {
            int dx = ctx.random().nextInt(16) - 8;
            int dz = ctx.random().nextInt(16) - 8;
            BlockPos pos = ctx.origin().offset(dx, 0, dz);
            pos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, pos);

            if (!level.getBlockState(pos).is(Blocks.SHORT_GRASS)) continue;

            var belowState = level.getBlockState(pos.below());
            for (var p : PatchProfiles.ALL) {
                if (belowState.is(p.patchCoreTop()) || belowState.is(p.patchEdgeTop())) {
                    level.setBlock(pos, p.shortGrassVariant().defaultBlockState(), 2);
                    if (ctx.random().nextFloat() < 0.20f) return true;
                    break;
                }
            }
        }
        return true;
    }
}
