package net.anatomyworld.harambefmod.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;

import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

import static net.anatomyworld.harambefmod.worldgen.ModWorldgenKeys.id;
import static net.anatomyworld.harambefmod.worldgen.PatchProfiles.*;

/**
 * One bootstrap that handles ALL profiles.
 * - Patches (surface rules) per profile.
 * - Short grass swap feature per profile via biome modifier.
 */
public final class PatchWorldgenBootstrap {

    /* --------------------- Resource keys per-profile --------------------- */

    private static ResourceKey<NormalNoise.NoiseParameters> keyNoise(String suffix, String name) {
        return ResourceKey.create(Registries.NOISE, id(name + "_" + suffix));
    }
    private static ResourceKey<NoiseGeneratorSettings> keySettings(String suffix) {
        return ResourceKey.create(Registries.NOISE_SETTINGS, id("settings_" + suffix));
    }
    private static ResourceKey<LevelStem> keyStem(String suffix) {
        return ResourceKey.create(Registries.LEVEL_STEM, id("level_stem_" + suffix));
    }
    private static ResourceKey<ConfiguredFeature<?, ?>> keyConfiguredFeature(String suffix) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, id("swap_short_grass_" + suffix));
    }
    private static ResourceKey<PlacedFeature> keyPlacedFeature(String suffix) {
        return ResourceKey.create(Registries.PLACED_FEATURE, id("swap_short_grass_" + suffix));
    }
    private static ResourceKey<net.neoforged.neoforge.common.world.BiomeModifier> keyBiomeModifier(String suffix) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("swap_short_grass_" + suffix));
    }

    /* --------------------- Public entry points for Datagen --------------------- */

    // Register per-profile noise parameter sets (mask, dither, frequency gate).
    public static void bootstrapNoises(BootstrapContext<NormalNoise.NoiseParameters> ctx) {
        for (Profile p : ALL) {
            // PATCH MASK: keep -8 — large blobby islands (size you liked)
            ctx.register(keyNoise(p.suffix(), "carotene_patches"),
                    new NormalNoise.NoiseParameters(-8, List.of(1.0, 1.0, 1.0, 1.0)));
            // DITHER: small variation along edges
            ctx.register(keyNoise(p.suffix(), "carotene_dither"),
                    new NormalNoise.NoiseParameters(0, List.of(1.0, 0.5, 0.25)));
            // GATE: reduce frequency (affects how OFTEN patches spawn, not their size)
            ctx.register(keyNoise(p.suffix(), "carotene_gate"),
                    new NormalNoise.NoiseParameters(-10, List.of(1.0, 0.5, 0.25)));
        }
    }

    // Build per-profile NoiseGeneratorSettings (choosing normal vs amplified via profile flags),
    // then drop in our surface rules.
    public static void bootstrapNoiseSettings(BootstrapContext<NoiseGeneratorSettings> ctx) {
        for (Profile p : ALL) {
            // Choose vanilla base (normal/large/amplified) per profile
            final NoiseGeneratorSettings vanilla = NoiseGeneratorSettings.overworld(
                    ctx,
                    p.largeOverworld(),   // large biomes?
                    p.amplified()         // amplified terrain?
            );

            final SurfaceRules.RuleSource customTop = SurfaceRules.sequence(
                    // 1) Sand→Red Sand on the whole footprint (above water, sandy biomes)
                    redSandInsideFootprint(p),
                    // 2) 3-band patch (core/dither/ring), with 1-layer underlay under BOTH core and ring,
                    //    and dither that blends core vs edge.
                    threeBandPatch(p),
                    // fallback:
                    vanilla.surfaceRule()
            );

            final NoiseGeneratorSettings ours = new NoiseGeneratorSettings(
                    vanilla.noiseSettings(),
                    vanilla.defaultBlock(),
                    vanilla.defaultFluid(),
                    vanilla.noiseRouter(),
                    customTop,
                    vanilla.spawnTarget(),
                    vanilla.seaLevel(),
                    vanilla.disableMobGeneration(),
                    vanilla.aquifersEnabled(),
                    vanilla.oreVeinsEnabled(),
                    vanilla.useLegacyRandomSource()
            );

            ctx.register(keySettings(p.suffix()), ours);
        }
    }

    // Register LevelStems: vanilla Overworld dimension type + OVERWORLD biome params + our settings.
    public static void bootstrapLevelStems(BootstrapContext<LevelStem> ctx) {
        final HolderGetter<DimensionType> dimTypes = ctx.lookup(Registries.DIMENSION_TYPE);
        final HolderGetter<NoiseGeneratorSettings> settings = ctx.lookup(Registries.NOISE_SETTINGS);
        final HolderGetter<MultiNoiseBiomeSourceParameterList> paramLists = ctx.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);

        final Holder.Reference<DimensionType> overworldType = dimTypes.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
        final Holder.Reference<MultiNoiseBiomeSourceParameterList> overworldParams =
                paramLists.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);

        for (Profile p : ALL) {
            final MultiNoiseBiomeSource biomeSource = MultiNoiseBiomeSource.createFromPreset(overworldParams);
            final NoiseBasedChunkGenerator chunkGenerator = new NoiseBasedChunkGenerator(
                    biomeSource, settings.getOrThrow(keySettings(p.suffix()))
            );
            ctx.register(keyStem(p.suffix()), new LevelStem(overworldType, chunkGenerator));
        }
    }

    // ConfiguredFeature -> our small swapper feature
    public static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> ctx) {
        for (Profile p : ALL) {
            ctx.register(
                    keyConfiguredFeature(p.suffix()),
                    new ConfiguredFeature<>(SwapShortGrassFeature.INSTANCE, NoneFeatureConfiguration.INSTANCE)
            );
        }
    }

    // PlacedFeature -> run swapper a bunch across the chunk surface (heightmap, in-square spread)
    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> ctx) {
        final HolderGetter<ConfiguredFeature<?, ?>> configured = ctx.lookup(Registries.CONFIGURED_FEATURE);
        for (Profile p : ALL) {
            ctx.register(
                    keyPlacedFeature(p.suffix()),
                    new PlacedFeature(
                            configured.getOrThrow(keyConfiguredFeature(p.suffix())),
                            List.of(
                                    InSquarePlacement.spread(),
                                    PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                                    CountPlacement.of(128) // tune if too light/heavy
                            )
                    )
            );
        }
    }

    // BiomeModifiers -> add our placed feature to all overworld biomes in the vegetation step.
    public static void bootstrapBiomeModifiers(BootstrapContext<net.neoforged.neoforge.common.world.BiomeModifier> ctx) {
        final HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);
        final HolderGetter<PlacedFeature> placed = ctx.lookup(Registries.PLACED_FEATURE);

        final HolderSet<Biome> overworldBiomes = biomes.getOrThrow(BiomeTags.IS_OVERWORLD);

        for (Profile p : ALL) {
            ctx.register(
                    keyBiomeModifier(p.suffix()),
                    new BiomeModifiers.AddFeaturesBiomeModifier(
                            overworldBiomes,
                            HolderSet.direct(placed.getOrThrow(keyPlacedFeature(p.suffix()))),
                            GenerationStep.Decoration.VEGETAL_DECORATION // single enum, not a Set
                    )
            );
        }
    }

    /* --------------------- Surface rule builders (per-profile) --------------------- */

    // Red sand on *entire* footprint (core+dither+ring) when sandy biomes & above water.
    private static SurfaceRules.RuleSource redSandInsideFootprint(Profile p) {
        var onFloor    = SurfaceRules.stoneDepthCheck(0, false, CaveSurface.FLOOR);
        var aboveWater = SurfaceRules.waterStartCheck(0, 0);
        var sandyBiomes = SurfaceRules.isBiome(
                net.minecraft.world.level.biome.Biomes.BEACH,
                net.minecraft.world.level.biome.Biomes.SNOWY_BEACH,
                net.minecraft.world.level.biome.Biomes.WARM_OCEAN,
                net.minecraft.world.level.biome.Biomes.LUKEWARM_OCEAN,
                net.minecraft.world.level.biome.Biomes.DEEP_LUKEWARM_OCEAN,
                net.minecraft.world.level.biome.Biomes.DESERT
        );

        var mask = keyNoise(p.suffix(), "carotene_patches");
        var gate = keyNoise(p.suffix(), "carotene_gate");

        var inGate   = SurfaceRules.noiseCondition(gate, -0.15D, 0.60D);
        var core     = SurfaceRules.noiseCondition(mask, -0.22D,  0.22D);
        var ditherLo = SurfaceRules.noiseCondition(mask, -0.38D, -0.22D);
        var ditherHi = SurfaceRules.noiseCondition(mask,  0.22D,  0.38D);
        var ringLo   = SurfaceRules.noiseCondition(mask, -0.50D, -0.38D);
        var ringHi   = SurfaceRules.noiseCondition(mask,  0.38D,  0.50D);

        var redSand = SurfaceRules.state(Blocks.RED_SAND.defaultBlockState());

        return SurfaceRules.ifTrue(
                SurfaceRules.abovePreliminarySurface(),
                SurfaceRules.ifTrue(aboveWater,
                        SurfaceRules.ifTrue(inGate,
                                SurfaceRules.ifTrue(sandyBiomes,
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(core,     SurfaceRules.ifTrue(onFloor, redSand)),
                                                SurfaceRules.ifTrue(ditherLo, SurfaceRules.ifTrue(onFloor, redSand)),
                                                SurfaceRules.ifTrue(ditherHi, SurfaceRules.ifTrue(onFloor, redSand)),
                                                SurfaceRules.ifTrue(ringLo,   SurfaceRules.ifTrue(onFloor, redSand)),
                                                SurfaceRules.ifTrue(ringHi,   SurfaceRules.ifTrue(onFloor, redSand))
                                        )
                                )
                        )
                )
        );
    }

    // Three-band patch with dither mix and 1-layer underlay beneath both core & ring.
    private static SurfaceRules.RuleSource threeBandPatch(Profile p) {
        var onFloor    = SurfaceRules.stoneDepthCheck(0, false, CaveSurface.FLOOR);
        var floor1     = SurfaceRules.stoneDepthCheck(1, false, CaveSurface.FLOOR);
        var aboveWater = SurfaceRules.waterStartCheck(0, 0);

        var mask   = keyNoise(p.suffix(), "carotene_patches");
        var dither = keyNoise(p.suffix(), "carotene_dither");
        var gate   = keyNoise(p.suffix(), "carotene_gate");

        var inGate   = SurfaceRules.noiseCondition(gate, -0.15D, 0.60D);
        var core     = SurfaceRules.noiseCondition(mask, -0.22D,  0.22D);
        var ditherLo = SurfaceRules.noiseCondition(mask, -0.38D, -0.22D);
        var ditherHi = SurfaceRules.noiseCondition(mask,  0.22D,  0.38D);
        var ringLo   = SurfaceRules.noiseCondition(mask, -0.50D, -0.38D);
        var ringHi   = SurfaceRules.noiseCondition(mask,  0.38D,  0.50D);

        var ditherFavorCore = SurfaceRules.noiseCondition(dither, 0.05D, Double.MAX_VALUE);

        var coreTop   = SurfaceRules.state(p.coreTop().defaultBlockState());
        var edgeTop   = SurfaceRules.state(p.edgeTop().defaultBlockState());
        var underLay  = SurfaceRules.state(p.underBlock().defaultBlockState());

        var coreRule = SurfaceRules.sequence(
                SurfaceRules.ifTrue(onFloor, coreTop),
                SurfaceRules.ifTrue(floor1,  underLay)
        );

        var ditherBlend = SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        ditherFavorCore,
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(onFloor, coreTop),
                                SurfaceRules.ifTrue(floor1,  underLay)
                        )
                ),
                SurfaceRules.ifTrue(onFloor, edgeTop),
                SurfaceRules.ifTrue(floor1,  underLay)
        );

        var ringRule = SurfaceRules.sequence(
                SurfaceRules.ifTrue(onFloor, edgeTop),
                SurfaceRules.ifTrue(floor1,  underLay)
        );

        var notSandy = SurfaceRules.not(SurfaceRules.isBiome(
                net.minecraft.world.level.biome.Biomes.BEACH,
                net.minecraft.world.level.biome.Biomes.SNOWY_BEACH,
                net.minecraft.world.level.biome.Biomes.WARM_OCEAN,
                net.minecraft.world.level.biome.Biomes.LUKEWARM_OCEAN,
                net.minecraft.world.level.biome.Biomes.DEEP_LUKEWARM_OCEAN,
                net.minecraft.world.level.biome.Biomes.DESERT
        ));

        return SurfaceRules.ifTrue(
                SurfaceRules.abovePreliminarySurface(),
                SurfaceRules.ifTrue(aboveWater,
                        SurfaceRules.ifTrue(inGate,
                                SurfaceRules.ifTrue(notSandy,
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(core,     coreRule),
                                                SurfaceRules.ifTrue(ditherLo, ditherBlend),
                                                SurfaceRules.ifTrue(ditherHi, ditherBlend),
                                                SurfaceRules.ifTrue(ringLo,   ringRule),
                                                SurfaceRules.ifTrue(ringHi,   ringRule)
                                        )
                                )
                        )
                )
        );
    }

    private PatchWorldgenBootstrap() {}
}
