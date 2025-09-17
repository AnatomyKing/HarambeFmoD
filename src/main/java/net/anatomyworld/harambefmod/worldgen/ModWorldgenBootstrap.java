package net.anatomyworld.harambefmod.worldgen;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

import java.util.List;

import static net.anatomyworld.harambefmod.worldgen.ModWorldgenKeys.*;

public final class ModWorldgenBootstrap {

    /* ---------- NOISES (for patch masks) ---------- */
    public static void bootstrapNoises(BootstrapContext<NormalNoise.NoiseParameters> ctx) {
        // 1.21.x: use List<Double> ctor
        ctx.register(CAROTENE_PATCHES, new NormalNoise.NoiseParameters(-8, List.of(1.0, 1.0, 1.0, 1.0)));
        ctx.register(RED_SAND_PATCHES,  new NormalNoise.NoiseParameters(-7, List.of(1.0, 1.0, 1.0)));
    }

    /* ---------- NOISE SETTINGS (copy Overworld, replace surface rule) ---------- */
    public static void bootstrapNoiseSettings(BootstrapContext<NoiseGeneratorSettings> ctx) {
        // Build a fresh "vanilla overworld" instance inside THIS registry set
        // (works in datagen; no unbound holder lookups)
        final NoiseGeneratorSettings vanilla = NoiseGeneratorSettings.overworld(ctx, /*large*/false, /*amplified*/false);

        // Our custom top rule: carotene grass mega-patches (with 3 layers of podzol) + red sand patches,
        // falling back to vanilla surface rule for everything else.
        final SurfaceRules.RuleSource customTop = SurfaceRules.sequence(
                carotenePatchRule(),
                redSandPatchRule(),
                vanilla.surfaceRule()
        );

        final NoiseGeneratorSettings ours = new NoiseGeneratorSettings(
                vanilla.noiseSettings(),        // terrain shape
                vanilla.defaultBlock(),         // stone
                vanilla.defaultFluid(),         // water
                vanilla.noiseRouter(),          // KEEP router to avoid aquifer "fluid_level_*" errors
                customTop,                      // our surface rule
                vanilla.spawnTarget(),          // spawn targets
                vanilla.seaLevel(),             // sea level
                vanilla.disableMobGeneration(), // keep flags (deprecation is fine here)
                vanilla.aquifersEnabled(),
                vanilla.oreVeinsEnabled(),
                vanilla.useLegacyRandomSource()
        );

        ctx.register(OVERWORLDNEW_SETTINGS, ours);
    }

    /* ---------- DIMENSION (LevelStem) ---------- */
    public static void bootstrapLevelStems(BootstrapContext<LevelStem> ctx) {
        final HolderGetter<DimensionType> dimTypes = ctx.lookup(Registries.DIMENSION_TYPE);
        final HolderGetter<NoiseGeneratorSettings> settings = ctx.lookup(Registries.NOISE_SETTINGS);
        final HolderGetter<MultiNoiseBiomeSourceParameterList> paramLists = ctx.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);

        // 1.21.x: Overworld DimensionType key lives in BuiltinDimensionTypes
        final var overworldType = dimTypes.getOrThrow(BuiltinDimensionTypes.OVERWORLD);

        // 1.21.x: MultiNoiseBiomeSource preset is a separate registry; build from the OVERWORLD parameter list
        final var overworldParams = paramLists.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
        final MultiNoiseBiomeSource biomeSource = MultiNoiseBiomeSource.createFromPreset(overworldParams);

        // Standard noise chunk generator with our custom settings
        final var chunkGenerator = new NoiseBasedChunkGenerator(
                biomeSource,
                settings.getOrThrow(OVERWORLDNEW_SETTINGS)
        );

        ctx.register(OVERWORLDNEW, new LevelStem(overworldType, chunkGenerator));
    }

    /* ---------- helpers: our surface rules ---------- */

    private static SurfaceRules.RuleSource carotenePatchRule() {
        // Place only above preliminary surface (top few layers)
        var onFloor = SurfaceRules.stoneDepthCheck(0, false, CaveSurface.FLOOR);
        var d1      = SurfaceRules.stoneDepthCheck(1, false, CaveSurface.FLOOR);
        var d2      = SurfaceRules.stoneDepthCheck(2, false, CaveSurface.FLOOR);
        var d3      = SurfaceRules.stoneDepthCheck(3, false, CaveSurface.FLOOR);

        // IMPORTANT: noiseCondition expects a ResourceKey<NoiseParameters> in 1.21.x
        return SurfaceRules.ifTrue(
                SurfaceRules.abovePreliminarySurface(),
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition(CAROTENE_PATCHES, -0.20D, 0.20D),
                        SurfaceRules.sequence(
                                // top block in the patch
                                SurfaceRules.ifTrue(onFloor,
                                        SurfaceRules.state(ModBlocks.CAROTENE_GRASS_BLOCK.get().defaultBlockState())),
                                // 3 layers under it = podzol
                                SurfaceRules.ifTrue(d1, SurfaceRules.state(Blocks.PODZOL.defaultBlockState())),
                                SurfaceRules.ifTrue(d2, SurfaceRules.state(Blocks.PODZOL.defaultBlockState())),
                                SurfaceRules.ifTrue(d3, SurfaceRules.state(Blocks.PODZOL.defaultBlockState()))
                        )
                )
        );
    }

    private static SurfaceRules.RuleSource redSandPatchRule() {
        var floor = SurfaceRules.stoneDepthCheck(0, false, CaveSurface.FLOOR);

        // Where vanilla would pick sand, sometimes swap to red sand
        // (beaches + warm/lukewarm oceans + deserts)
        var sandBiomes = SurfaceRules.isBiome(
                Biomes.BEACH, Biomes.SNOWY_BEACH,
                Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, // <- exact case
                Biomes.DESERT
        );

        return SurfaceRules.ifTrue(
                SurfaceRules.abovePreliminarySurface(),
                SurfaceRules.ifTrue(
                        sandBiomes,
                        SurfaceRules.ifTrue(
                                SurfaceRules.noiseCondition(RED_SAND_PATCHES, 0.25D, 1.0D),
                                SurfaceRules.ifTrue(floor, SurfaceRules.state(Blocks.RED_SAND.defaultBlockState()))
                        )
                )
        );
    }

    private ModWorldgenBootstrap() {}
}
