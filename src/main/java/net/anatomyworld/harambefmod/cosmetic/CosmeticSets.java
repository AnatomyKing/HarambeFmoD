package net.anatomyworld.harambefmod.cosmetic;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.network.SyncCosmeticSetsPayload;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads JSON cosmetics from data/<ns>/cosmetics/sets/*.json and keeps them in memory.
 * Example: data/harambefmod/cosmetics/sets/netherite_fake.json -> harambefmod:netherite_fake
 */
public final class CosmeticSets extends SimpleJsonResourceReloadListener<CosmeticSet> {
    private static final String FOLDER = "cosmetics/sets";

    // Thread-safe because reload runs off-thread
    private static final Map<ResourceLocation, CosmeticSet> SETS = new ConcurrentHashMap<>();

    /** 1.21.4+: pass a Codec and a FileToIdConverter */
    public CosmeticSets() {
        super(CosmeticSet.CODEC.codec(), FileToIdConverter.json(FOLDER));
    }

    /** 1.21.4+: decoded map of id -> CosmeticSet arrives here */
    @Override
    protected void apply(Map<ResourceLocation, CosmeticSet> decoded, ResourceManager mgr, ProfilerFiller profiler) {
        SETS.clear();
        SETS.putAll(decoded);
        HarambeCore.LOGGER.info("Loaded {} cosmetic sets from '{}'", SETS.size(), FOLDER);
    }

    public static Map<ResourceLocation, CosmeticSet> all() {
        return Collections.unmodifiableMap(SETS);
    }

    public static CosmeticSet get(ResourceLocation id) {
        return SETS.get(id);
    }

    /* -------------------------
       Registration & Syncing
       ------------------------- */

    /** Register this as a *server* reload listener (MOD bus). */
    public static void addServerReloaders(AddServerReloadListenersEvent event) {
        // Supply an ID because this is a SortedReloadListener event
        event.addListener(ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "cosmetic_sets"), new CosmeticSets());
    }

    /**
     * Sync to clients when a player joins OR after /reload (GAME bus).
     * If player is null, broadcast to everyone.
     */
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            syncTo(event.getPlayer());
        } else {
            for (ServerPlayer sp : event.getPlayerList().getPlayers()) {
                syncTo(sp);
            }
        }
    }

    /** Send all sets to one player. */
    public static void syncTo(ServerPlayer player) {
        var entries = SETS.entrySet().stream()
                .map(e -> new SyncCosmeticSetsPayload.Entry(e.getKey(), e.getValue()))
                .toList();
        PacketDistributor.sendToPlayer(player, new SyncCosmeticSetsPayload(entries));
    }
}
