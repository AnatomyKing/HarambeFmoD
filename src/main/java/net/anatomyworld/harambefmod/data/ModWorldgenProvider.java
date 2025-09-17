package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.worldgen.PatchWorldgenBootstrap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Datagen: registers worldgen for ALL patch profiles. */
public final class ModWorldgenProvider extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()

            .add(Registries.NOISE_SETTINGS, ctx -> {
                NoiseGeneratorSettings.bootstrap(ctx);
                PatchWorldgenBootstrap.bootstrapNoiseSettings(ctx);
            })
            // Noises for mask/dither/gate (per profile)
            .add(Registries.NOISE, PatchWorldgenBootstrap::bootstrapNoises)
            // Our dimension stems (per profile) built from vanilla overworld type + overworld biome preset
            .add(Registries.LEVEL_STEM, PatchWorldgenBootstrap::bootstrapLevelStems)
            // Feature + placed feature that swaps short grass in patch footprint
            .add(Registries.CONFIGURED_FEATURE, PatchWorldgenBootstrap::bootstrapConfiguredFeatures)
            .add(Registries.PLACED_FEATURE, PatchWorldgenBootstrap::bootstrapPlacedFeatures)
            // Biome modifier that adds our placed feature to overworld biomes
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, PatchWorldgenBootstrap::bootstrapBiomeModifiers);

    public ModWorldgenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, BUILDER, Set.of(HarambeCore.MOD_ID));
    }
}
