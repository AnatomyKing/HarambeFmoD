package net.anatomyworld.harambefmod.economy;

import com.mojang.authlib.GameProfile;
import net.anatomyworld.harambefmod.network.BalanceSyncPayload;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

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

    /** Adds money and pushes a live HUD update if the player is online. */
    public static long add(MinecraftServer server, GameProfile profile, long amount) {
        UUID id = uuidOf(profile);
        long after = EconomyData.get(server).add(id, amount);
        syncIfOnline(server, id, after);
        return after;
    }

    /** Removes money and pushes a live HUD update if the player is online. */
    public static long remove(MinecraftServer server, GameProfile profile, long amount) {
        UUID id = uuidOf(profile);
        long after = EconomyData.get(server).remove(id, amount);
        syncIfOnline(server, id, after);
        return after;
    }

    /** Sets the balance and pushes a live HUD update if the player is online. */
    public static void set(MinecraftServer server, GameProfile profile, long value) {
        UUID id = uuidOf(profile);
        long clamped = Math.max(0L, value);
        EconomyData.get(server).setBalance(id, clamped);
        syncIfOnline(server, id, clamped);
    }

    /** If the player is online, send a BalanceSyncPayload so the HUD updates immediately. */
    private static void syncIfOnline(MinecraftServer server, UUID playerId, long balance) {
        ServerPlayer sp = server.getPlayerList().getPlayer(playerId);
        if (sp != null) {
            PacketDistributor.sendToPlayer(sp, new BalanceSyncPayload(balance));
        }
    }
}
