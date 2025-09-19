// src/main/java/net/anatomyworld/harambefmod/event/PortalMemory.java
package net.anatomyworld.harambefmod.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public final class PortalMemory {
    private PortalMemory() {}
    private static final String TAG = "harambefmod:last_overworld_like";

    public static void rememberIfOverworldLike(Entity entity, ResourceKey<Level> dim, boolean force) {
        if (force || net.anatomyworld.harambefmod.Config.isOverworldLike(dim)) {
            // Always store a plain, valid string id
            String id = dim.location().toString(); // e.g. "minecraft:overworld"
            entity.getPersistentData().putString(TAG, id);
        }
    }

    public static Optional<ResourceKey<Level>> get(Entity entity) {
        CompoundTag tag = entity.getPersistentData();
        if (!tag.contains(TAG)) return Optional.empty();

        String raw = String.valueOf(tag.getString(TAG));
        if (raw == null) return Optional.empty();
        raw = raw.trim();
        // Migrate legacy/bad values like "Optional[minecraft:overworld]"
        if (raw.startsWith("Optional[")) {
            int open = raw.indexOf('[');
            int close = raw.lastIndexOf(']');
            if (open >= 0 && close > open) raw = raw.substring(open + 1, close);
        }

        ResourceLocation rl = ResourceLocation.tryParse(raw);
        if (rl == null) {
            // Invalid -> clean it so it never crashes again
            tag.remove(TAG);
            return Optional.empty();
        }
        return Optional.of(ResourceKey.create(Registries.DIMENSION, rl));
    }
}
