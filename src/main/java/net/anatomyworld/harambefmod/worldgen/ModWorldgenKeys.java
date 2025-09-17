package net.anatomyworld.harambefmod.worldgen;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.resources.ResourceLocation;

public final class ModWorldgenKeys {
    private ModWorldgenKeys() {}

    /** 1.21+: use the factory (constructors are private). */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, path);
    }
}
