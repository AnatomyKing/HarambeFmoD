package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.worldgen.WorldgenBootstrap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class ModWorldgenProvider extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.NOISE_SETTINGS, ctx -> {
                NoiseGeneratorSettings.bootstrap(ctx);           // vanilla first
                WorldgenBootstrap.bootstrapNoiseSettings(ctx);   // ours
            })
            .add(Registries.NOISE, WorldgenBootstrap::bootstrapNoises)
            .add(Registries.LEVEL_STEM, WorldgenBootstrap::bootstrapLevelStems)
            .add(Registries.CONFIGURED_FEATURE, WorldgenBootstrap::bootstrapConfiguredFeatures)
            .add(Registries.PLACED_FEATURE, WorldgenBootstrap::bootstrapPlacedFeatures)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, WorldgenBootstrap::bootstrapBiomeModifiers);

    public ModWorldgenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, BUILDER, Set.of(HarambeCore.MOD_ID));
    }
}
