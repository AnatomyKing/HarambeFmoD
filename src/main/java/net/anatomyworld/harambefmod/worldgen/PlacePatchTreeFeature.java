package net.anatomyworld.harambefmod.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import static net.anatomyworld.harambefmod.worldgen.TreeStyles.BY_STYLE;

/** Place a profile-chosen tree style, but only when standing on your patch top blocks. */
public final class PlacePatchTreeFeature extends Feature<NoneFeatureConfiguration> {
    public PlacePatchTreeFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        var rand = ctx.random();

        boolean any = false;
        for (int tries = 0; tries < 8; tries++) {
            int dx = rand.nextInt(16) - 8;
            int dz = rand.nextInt(16) - 8;
            BlockPos pos = ctx.origin().offset(dx, 0, dz);
            pos = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, pos);

            var belowState = level.getBlockState(pos.below());
            if (level.getBlockState(pos).is(Blocks.WATER)) continue;

            for (var p : PatchProfiles.ALL) {
                if (!(belowState.is(p.patchCoreTop()) || belowState.is(p.patchEdgeTop()))) continue;

                if (p.treeStyle() == null) continue;
                var builder = BY_STYLE.get(p.treeStyle());
                if (builder != null && builder.place(level, pos, rand)) {
                    any = true;
                    if (rand.nextFloat() < 0.35f) return true; // throttle
                }
            }
        }
        return any;
    }
}
