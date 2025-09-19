package net.anatomyworld.harambefmod.worldgen;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.anatomyworld.harambefmod.worldgen.ModWorldgenKeys.id;

public final class ModFeatures {
    private ModFeatures() {}

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, HarambeCore.MOD_ID);

    public static final ResourceKey<Feature<?>> SWAP_SHORT_GRASS_KEY =
            ResourceKey.create(Registries.FEATURE, id("swap_short_grass"));
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> SWAP_SHORT_GRASS =
            FEATURES.register("swap_short_grass", SwapShortGrassFeature::new);

    public static final ResourceKey<Feature<?>> PATCH_TREE_KEY =
            ResourceKey.create(Registries.FEATURE, id("patch_tree"));
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> PATCH_TREE =
            FEATURES.register("patch_tree", PlacePatchTreeFeature::new);
}
