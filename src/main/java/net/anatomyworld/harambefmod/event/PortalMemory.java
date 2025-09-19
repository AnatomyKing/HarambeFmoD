// src/main/java/net/anatomyworld/harambefmod/portal/PortalMemory.java
package net.anatomyworld.harambefmod.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Optional;

public final class PortalMemory {
    private static final String TAG = "harambefmod:last_overworld_like";

    public static void rememberIfOverworldLike(ServerPlayer player, ResourceKey<Level> dim, boolean force) {
        if (force || net.anatomyworld.harambefmod.Config.isOverworldLike(dim)) {
            CompoundTag tag = player.getPersistentData();
            tag.putString(TAG, dim.location().toString());
        }
    }

    public static Optional<ResourceKey<Level>> get(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains(TAG)) return Optional.empty();
        String id = tag.getString(TAG);
        if (id.isBlank()) return Optional.empty();
        return Optional.of(ResourceKey.create(Level.RESOURCE_KEY, new ResourceLocation(id)));
    }
}
