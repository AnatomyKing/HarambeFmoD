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
            entity.getPersistentData().putString(TAG, dim.location().toString());
        }
    }

    public static Optional<ResourceKey<Level>> get(Entity entity) {
        CompoundTag tag = entity.getPersistentData();
        if (!tag.contains(TAG)) return Optional.empty();

        String raw = String.valueOf(tag.getString(TAG));
        if (raw == null || raw.isBlank()) return Optional.empty();

        // Migrate legacy "Optional[namespace:id]" forms, just in case
        if (raw.startsWith("Optional[")) {
            int open = raw.indexOf('[');
            int close = raw.lastIndexOf(']');
            if (open >= 0 && close > open) raw = raw.substring(open + 1, close);
        }

        ResourceLocation rl = ResourceLocation.tryParse(raw.trim());
        if (rl == null) {
            tag.remove(TAG);
            return Optional.empty();
        }
        return Optional.of(ResourceKey.create(Registries.DIMENSION, rl));
    }
}
