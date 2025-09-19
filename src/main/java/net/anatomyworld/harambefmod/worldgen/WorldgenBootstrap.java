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

    /* ---------- noises for your surface rules ---------- */
    public static void bootstrapNoises(BootstrapContext<NormalNoise.NoiseParameters> ctx) {
        for (var p : ALL) {
            ctx.register(keyNoise(p.suffix(), "carotene_patches"),
                    new NormalNoise.NoiseParameters(-8, List.of(1.0, 1.0, 1.0, 1.0)));
            ctx.register(keyNoise(p.suffix(), "carotene_dither"),
                    new NormalNoise.NoiseParameters(0, List.of(1.0, 0.5, 0.25)));
            ctx.register(keyNoise(p.suffix(), "carotene_gate"),
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
            if (p.enableSandyRedSand()) overlay.add(redSandInsideFootprint(p));
            overlay.add(threeBandPatch(p));

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
            final Holder<NoiseGeneratorSettings> chosen;
            if (p.customNoiseSettingsId() == null) {
                chosen = settings.getOrThrow(keySettings(p.suffix()));
            } else {
                chosen = settings.getOrThrow(ResourceKey.create(Registries.NOISE_SETTINGS, p.customNoiseSettingsId()));
            }

            var biomeSource = MultiNoiseBiomeSource.createFromPreset(overworldParams);
            var chunkGen = new NoiseBasedChunkGenerator(biomeSource, chosen);
            ctx.register(keyStem(p.suffix()), new LevelStem(overworldType, chunkGen));
        }
    }

    /* ---------- configured / placed features (derived from profile config) ---------- */
    public static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> ctx) {
        HolderGetter<Feature<?>> features = ctx.lookup(Registries.FEATURE);

        for (var p : ALL) {
            // --- swap_short_grass (always) ---
            {
                @SuppressWarnings("unchecked")
                Feature<NoneFeatureConfiguration> feat =
                        (Feature<NoneFeatureConfiguration>) features.getOrThrow(ModFeatures.SWAP_SHORT_GRASS_KEY).value();

                ctx.register(
                        keyConfiguredFeature(p.suffix(), "swap_short_grass"),
                        new ConfiguredFeature<>(feat, NoneFeatureConfiguration.INSTANCE)
                );
            }

            // --- optional: patch_tree ---
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
            // grass swap — fixed-ish density (tweak if you want)
            ctx.register(
                    keyPlacedFeature(p.suffix(), "swap_short_grass"),
                    new PlacedFeature(
                            configured.getOrThrow(keyConfiguredFeature(p.suffix(), "swap_short_grass")),
                            List.of(
                                    InSquarePlacement.spread(),
                                    PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                                    CountPlacement.of(96),
                                    BiomeFilter.biome()
                            )
                    )
            );

            // trees — per-profile rarity + count
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

    /* ---------- biome modifiers: inject the placed features that actually exist ---------- */
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

    /* ---------- your SURFACE RULES (shared) ---------- */

    public static SurfaceRules.RuleSource redSandInsideFootprint(PatchProfiles.Profile p) {
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

        var sandBlock = (p.sandyRedSandBlock() != null ? p.sandyRedSandBlock() : Blocks.RED_SAND).defaultBlockState();
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

    public static SurfaceRules.RuleSource threeBandPatch(PatchProfiles.Profile p) {
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
