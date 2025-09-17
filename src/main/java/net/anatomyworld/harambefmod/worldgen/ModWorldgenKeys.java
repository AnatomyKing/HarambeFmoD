package net.anatomyworld.harambefmod.worldgen;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class ModWorldgenKeys {

    // Custom noises used by our surface rules
    public static final ResourceKey<NormalNoise.NoiseParameters> CAROTENE_PATCHES =
            ResourceKey.create(Registries.NOISE, id("carotene_patches"));
    public static final ResourceKey<NormalNoise.NoiseParameters> RED_SAND_PATCHES =
            ResourceKey.create(Registries.NOISE, id("red_sand_patches"));

    // Our noise settings & dimension (level stem)
    public static final ResourceKey<NoiseGeneratorSettings> OVERWORLDNEW_SETTINGS =
            ResourceKey.create(Registries.NOISE_SETTINGS, id("overworldnew"));
    public static final ResourceKey<LevelStem> OVERWORLDNEW =
            ResourceKey.create(Registries.LEVEL_STEM, id("overworldnew"));

    private static ResourceLocation id(String path) {
        // 1.21+: constructors are private; use the factory
        return ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, path);
    }

    private ModWorldgenKeys() {}
}
