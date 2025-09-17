package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.worldgen.ModWorldgenBootstrap;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class ModWorldgenProvider extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            // We need vanilla noise settings (provides minecraft:overworld, aquifers, etc.) …
            .add(Registries.NOISE_SETTINGS, ctx -> {
                NoiseGeneratorSettings.bootstrap(ctx);              // <- vanilla
                ModWorldgenBootstrap.bootstrapNoiseSettings(ctx);   // <- ours (overrides surface rules)
            })
            // Our custom noise parameters used by surface rules
            .add(Registries.NOISE, ModWorldgenBootstrap::bootstrapNoises)
            // Our dimension (LevelStem) using vanilla Overworld type + overworld biome preset
            .add(Registries.LEVEL_STEM, ModWorldgenBootstrap::bootstrapLevelStems);

    public ModWorldgenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, BUILDER, Set.of(HarambeCore.MOD_ID));
    }
}
