package net.anatomyworld.harambefmod.worldgen;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.resources.ResourceLocation;

public final class ModWorldgenKeys {
    private ModWorldgenKeys() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, path);
    }
}
