package net.anatomyworld.harambefmod.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import static net.anatomyworld.harambefmod.worldgen.ModWorldgenKeys.id;
import static net.anatomyworld.harambefmod.worldgen.PatchProfiles.ALL;

public final class WorldgenBootstrap {
    private WorldgenBootstrap() {}

    /* ---------- keys ---------- */

    private static ResourceKey<NormalNoise.NoiseParameters> keyNoise(String suffix, String name) {
        return ResourceKey.create(Registries.NOISE, id(name + "_" + suffix));
    }
    private static ResourceKey<ConfiguredFeature<?, ?>> keyConfiguredFeature(String suffix, String base) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, id(base + "_" + suffix));
    }
    private static ResourceKey<PlacedFeature> keyPlacedFeature(String suffix, String base) {
        return ResourceKey.create(Registries.PLACED_FEATURE, id(base + "_" + suffix));
    }
    private static ResourceKey<net.neoforged.neoforge.common.world.BiomeModifier> keyBiomeModifierAdd(String suffix) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("profile_features_add_" + suffix));
    }
    private static ResourceKey<NoiseGeneratorSettings> keySettings(String suffix) {
        return ResourceKey.create(Registries.NOISE_SETTINGS, id("settings_" + suffix));
    }
    private static ResourceKey<LevelStem> keyStem(String suffix) {
        return ResourceKey.create(Registries.LEVEL_STEM, id(suffix));
    }

    /* ---------- patch noises (renamed, generic) ---------- */
    public static void bootstrapNoises(BootstrapContext<NormalNoise.NoiseParameters> ctx) {
        for (var p : ALL) {
            // Keep parameters as before; sizing/coverage are applied via thresholds.
            ctx.register(keyNoise(p.suffix(), "patch_mask"),
                    new NormalNoise.NoiseParameters(-8, List.of(1.0, 1.0, 1.0, 1.0)));
            ctx.register(keyNoise(p.suffix(), "patch_dither"),
                    new NormalNoise.NoiseParameters(0, List.of(1.0, 0.5, 0.25)));
            ctx.register(keyNoise(p.suffix(), "patch_gate"),
                    new NormalNoise.NoiseParameters(-10, List.of(1.0, 0.5, 0.25)));
        }
    }

    /* ---------- noise settings (NORMAL default) + optional custom override per profile ---------- */
    public static void bootstrapNoiseSettings(BootstrapContext<NoiseGeneratorSettings> ctx) {
        for (var p : ALL) {
            final ResourceLocation custom = p.customNoiseSettingsId();
            if (custom != null) continue; // use provided settings as-is

            // Base = Mojang normal overworld
            NoiseGeneratorSettings baseNormal = NoiseGeneratorSettings.overworld(ctx, /*largeBiomes*/ false, /*amplified*/ false);

            // Build overlay sequence conditionally
            List<SurfaceRules.RuleSource> overlay = new ArrayList<>();
            if (p.enableBeachRedSand()) overlay.add(sandOverlayInsidePatch(p));
            overlay.add(patchSurfaceBands(p));

            var patches = SurfaceRules.sequence(overlay.toArray(SurfaceRules.RuleSource[]::new));
            var top = SurfaceRules.sequence(patches, baseNormal.surfaceRule());

            NoiseGeneratorSettings ours = new NoiseGeneratorSettings(
                    baseNormal.noiseSettings(),
                    baseNormal.defaultBlock(),
                    baseNormal.defaultFluid(),
                    baseNormal.noiseRouter(),
                    top,
                    baseNormal.spawnTarget(),
                    baseNormal.seaLevel(),
                    baseNormal.disableMobGeneration(),
                    baseNormal.aquifersEnabled(),
                    baseNormal.oreVeinsEnabled(),
                    baseNormal.useLegacyRandomSource()
            );

            ctx.register(keySettings(p.suffix()), ours);
        }
    }

    /* ---------- level stems ---------- */
    public static void bootstrapLevelStems(BootstrapContext<LevelStem> ctx) {
        final HolderGetter<DimensionType> dimTypes = ctx.lookup(Registries.DIMENSION_TYPE);
        final HolderGetter<NoiseGeneratorSettings> settings = ctx.lookup(Registries.NOISE_SETTINGS);
        final HolderGetter<MultiNoiseBiomeSourceParameterList> params = ctx.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);

        final Holder.Reference<DimensionType> overworldType = dimTypes.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
        final Holder.Reference<MultiNoiseBiomeSourceParameterList> overworldParams =
                params.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);

        for (var p : ALL) {
            final Holder<NoiseGeneratorSettings> chosen =
                    settings.getOrThrow(p.customNoiseSettingsId() == null
                            ? keySettings(p.suffix())
                            : ResourceKey.create(Registries.NOISE_SETTINGS, p.customNoiseSettingsId()));

            var biomeSource = MultiNoiseBiomeSource.createFromPreset(overworldParams);
            var chunkGen = new NoiseBasedChunkGenerator(biomeSource, chosen);
            ctx.register(keyStem(p.suffix()), new LevelStem(overworldType, chunkGen));
        }
    }

    /* ---------- configured / placed features (derived from profile config) ---------- */
    public static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> ctx) {
        HolderGetter<Feature<?>> features = ctx.lookup(Registries.FEATURE);

        for (var p : ALL) {
            // swap_short_grass (always)
            {
                @SuppressWarnings("unchecked")
                Feature<NoneFeatureConfiguration> feat =
                        (Feature<NoneFeatureConfiguration>) features.getOrThrow(ModFeatures.SWAP_SHORT_GRASS_KEY).value();

                ctx.register(
                        keyConfiguredFeature(p.suffix(), "swap_short_grass"),
                        new ConfiguredFeature<>(feat, NoneFeatureConfiguration.INSTANCE)
                );
            }

            // optional: patch_tree
            boolean treesEnabled = p.treeStyle() != null && p.treeRarity() > 0 && p.treeCountPerRun() > 0;
            if (treesEnabled) {
                @SuppressWarnings("unchecked")
                Feature<NoneFeatureConfiguration> feat =
                        (Feature<NoneFeatureConfiguration>) features.getOrThrow(ModFeatures.PATCH_TREE_KEY).value();

                ctx.register(
                        keyConfiguredFeature(p.suffix(), "patch_tree"),
                        new ConfiguredFeature<>(feat, NoneFeatureConfiguration.INSTANCE)
                );
            }
        }
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> ctx) {
        final HolderGetter<ConfiguredFeature<?, ?>> configured = ctx.lookup(Registries.CONFIGURED_FEATURE);

        for (var p : ALL) {
            // Scale the grass swap pass density with patchCoverage (simple & intuitive)
            int base = 96;
            int count = Math.max(4, Math.round(base * clamp(p.patchCoverage(), 0.50f, 1.50f)));

            ctx.register(
                    keyPlacedFeature(p.suffix(), "swap_short_grass"),
                    new PlacedFeature(
                            configured.getOrThrow(keyConfiguredFeature(p.suffix(), "swap_short_grass")),
                            List.of(
                                    InSquarePlacement.spread(),
                                    PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                                    CountPlacement.of(count),
                                    BiomeFilter.biome()
                            )
                    )
            );

            // trees — per-profile rarity + count (unchanged)
            boolean treesEnabled = p.treeStyle() != null && p.treeRarity() > 0 && p.treeCountPerRun() > 0;
            if (treesEnabled) {
                ctx.register(
                        keyPlacedFeature(p.suffix(), "patch_tree"),
                        new PlacedFeature(
                                configured.getOrThrow(keyConfiguredFeature(p.suffix(), "patch_tree")),
                                List.of(
                                        InSquarePlacement.spread(),
                                        PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                                        RarityFilter.onAverageOnceEvery(Math.max(1, p.treeRarity())),
                                        CountPlacement.of(Math.max(1, p.treeCountPerRun())),
                                        BiomeFilter.biome()
                                )
                        )
                );
            }
        }
    }

    /* ---------- biome modifiers: inject the placed features ---------- */
    public static void bootstrapBiomeModifiers(BootstrapContext<net.neoforged.neoforge.common.world.BiomeModifier> ctx) {
        final HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);
        final HolderGetter<PlacedFeature> placed = ctx.lookup(Registries.PLACED_FEATURE);
        final HolderSet<Biome> overworldBiomes = biomes.getOrThrow(BiomeTags.IS_OVERWORLD);

        for (var p : ALL) {
            List<Holder<PlacedFeature>> list = new ArrayList<>();
            list.add(placed.getOrThrow(keyPlacedFeature(p.suffix(), "swap_short_grass")));

            boolean treesEnabled = p.treeStyle() != null && p.treeRarity() > 0 && p.treeCountPerRun() > 0;
            if (treesEnabled) {
                list.add(placed.getOrThrow(keyPlacedFeature(p.suffix(), "patch_tree")));
            }

            ctx.register(
                    keyBiomeModifierAdd(p.suffix()),
                    new BiomeModifiers.AddFeaturesBiomeModifier(
                            overworldBiomes,
                            HolderSet.direct(list),
                            GenerationStep.Decoration.VEGETAL_DECORATION
                    )
            );
        }
    }

    /* ---------- Patch maths & surface rules ---------- */

    private static float clamp(float v, float lo, float hi) { return Math.max(lo, Math.min(hi, v)); }

    private record Windows(double core, double ditherStart, double ditherEnd, double ringStart, double ringEnd,
                           double gateMin, double gateMax) {}

    /** Compute thresholds from two knobs. */
    private static Windows computeWindows(PatchProfiles.Profile p) {
        // Size scaling (how thick the bands are). Clamp to a safe range.
        double s = clamp(p.patchSize(), 0.60f, 1.60f);

        // Coverage scaling (how wide the gate window is). Clamp to safe range.
        double c = clamp(p.patchCoverage(), 0.50f, 1.50f);

        // Base (old) breakpoints:
        // core = 0.22; dither = 0.22..0.38; ring = 0.38..0.50
        double core = 0.22 * s;
        double ditherStart = 0.22 * s;
        double ditherEnd   = 0.38 * s;
        double ringStart   = 0.38 * s;
        double ringEnd     = 0.50 * s;

        // Gate center ~ 0.225, half width ~0.375. Scale half-width by coverage.
        double gateCenter = 0.225;
        double halfBase   = 0.375 * c;
        double gateMin    = Math.max(-1.0, gateCenter - halfBase);
        double gateMax    = Math.min( 1.0, gateCenter + halfBase);

        return new Windows(core, ditherStart, ditherEnd, ringStart, ringEnd, gateMin, gateMax);
    }

    /** Beach/desert red-sand overlay inside patch footprint. */
    public static SurfaceRules.RuleSource sandOverlayInsidePatch(PatchProfiles.Profile p) {
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

        var mask = keyNoise(p.suffix(), "patch_mask");
        var gate = keyNoise(p.suffix(), "patch_gate");
        var w = computeWindows(p);

        var inGate   = SurfaceRules.noiseCondition(gate, w.gateMin, w.gateMax);
        var core     = SurfaceRules.noiseCondition(mask, -w.core,  w.core);
        var ditherLo = SurfaceRules.noiseCondition(mask, -w.ditherEnd,  -w.ditherStart);
        var ditherHi = SurfaceRules.noiseCondition(mask,  w.ditherStart,  w.ditherEnd);
        var ringLo   = SurfaceRules.noiseCondition(mask, -w.ringEnd,     -w.ringStart);
        var ringHi   = SurfaceRules.noiseCondition(mask,  w.ringStart,    w.ringEnd);

        var sandBlock = (p.beachRedSandBlock() != null ? p.beachRedSandBlock() : Blocks.RED_SAND).defaultBlockState();
        var redSand = SurfaceRules.state(sandBlock);

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

    /** Generic three-band patch surface (core/dither/ring) with per-profile tuning. */
    public static SurfaceRules.RuleSource patchSurfaceBands(PatchProfiles.Profile p) {
        var onFloor    = SurfaceRules.stoneDepthCheck(0, false, CaveSurface.FLOOR);
        var floor1     = SurfaceRules.stoneDepthCheck(1, false, CaveSurface.FLOOR);
        var aboveWater = SurfaceRules.waterStartCheck(0, 0);

        var mask   = keyNoise(p.suffix(), "patch_mask");
        var dither = keyNoise(p.suffix(), "patch_dither");
        var gate   = keyNoise(p.suffix(), "patch_gate");
        var w = computeWindows(p);

        var inGate   = SurfaceRules.noiseCondition(gate, w.gateMin, w.gateMax);
        var core     = SurfaceRules.noiseCondition(mask, -w.core,  w.core);
        var ditherLo = SurfaceRules.noiseCondition(mask, -w.ditherEnd,  -w.ditherStart);
        var ditherHi = SurfaceRules.noiseCondition(mask,  w.ditherStart,  w.ditherEnd);
        var ringLo   = SurfaceRules.noiseCondition(mask, -w.ringEnd,     -w.ringStart);
        var ringHi   = SurfaceRules.noiseCondition(mask,  w.ringStart,    w.ringEnd);

        var ditherFavorCore = SurfaceRules.noiseCondition(dither, 0.05D, Double.MAX_VALUE);

        var coreTop   = SurfaceRules.state(p.patchCoreTop().defaultBlockState());
        var edgeTop   = SurfaceRules.state(p.patchEdgeTop().defaultBlockState());
        var underLay  = SurfaceRules.state(p.patchUnderlay().defaultBlockState());

        var coreRule = SurfaceRules.sequence(
                SurfaceRules.ifTrue(onFloor, coreTop),
                SurfaceRules.ifTrue(floor1,  underLay)
        );
        var ditherBlend = SurfaceRules.sequence(
                SurfaceRules.ifTrue(ditherFavorCore, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(onFloor, coreTop),
                        SurfaceRules.ifTrue(floor1,  underLay)
                )),
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
}
