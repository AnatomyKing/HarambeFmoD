package net.anatomyworld.harambefmod.economy;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

/** Small helper facade for commands/other systems. */
public final class Economy {
    private Economy() {}

    /** Resolve a UUID for a profile (offline-safe). */
    public static UUID uuidOf(GameProfile profile) {
        UUID id = profile.getId();
        if (id != null) return id;
        String name = profile.getName() != null ? profile.getName() : "unknown";
        return UUIDUtil.createOfflinePlayerUUID(name);
    }

    public static long get(MinecraftServer server, GameProfile profile) {
        return EconomyData.get(server).getBalance(uuidOf(profile));
    }

    public static long add(MinecraftServer server, GameProfile profile, long amount) {
        return EconomyData.get(server).add(uuidOf(profile), amount);
    }

    public static long remove(MinecraftServer server, GameProfile profile, long amount) {
        return EconomyData.get(server).remove(uuidOf(profile), amount);
    }

    public static void set(MinecraftServer server, GameProfile profile, long value) {
        EconomyData.get(server).setBalance(uuidOf(profile), value);
    }
}
