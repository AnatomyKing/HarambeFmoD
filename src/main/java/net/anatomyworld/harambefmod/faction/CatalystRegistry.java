package net.anatomyworld.harambefmod.faction;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * In-memory registry of faction catalyst zones with a dynamic expiry.
 *
 * Zone shape: AXIS-ALIGNED CUBE on X/Z with full world height (matches overlay).
 *   Contains if |dx| <= RADIUS and |dz| <= RADIUS.
 *
 * IMPORTANT:
 * - When a catalyst is placed, its expiry should be 0 so protection/effects are OFF
 *   until time is added by bloom-fed XP deaths.
 * - Protection/effects are active only if expiresAtMs > now.
 */
public final class CatalystRegistry {
    private CatalystRegistry() {}

    /** Radius in blocks (matches protection + overlay). */
    public static final int RADIUS = 32;
    public static final int RADIUS_PLUS_ONE = RADIUS + 1;

    /** Optional reference cap (not enforced). */
    public static final long LIFETIME_MS = 72L * 60L * 60L * 1000L;

    /** Sculk-like “nearby death” radius (blocks). */
    public static final int BLOOM_RADIUS_BLOCKS = 8;

    /** Bloom bonuses (ms). */
    public static final long BONUS_HOSTILE_MS = 12_000L; // +12s
    public static final long BONUS_PASSIVE_MS = 6_000L;  // +6s

    /** Immutable entry for a catalyst zone. */
    public record Entry(BlockPos pos, Faction faction, long expiresAtMs) {}

    private static final Map<ResourceKey<Level>, List<Entry>> REG = new HashMap<>();

    /** Add or replace a catalyst entry. expiresAtMs may be 0 to start inactive. */
    public static void put(ResourceKey<Level> dim, BlockPos pos, Faction f, long expiresAtMs) {
        List<Entry> list = REG.computeIfAbsent(dim, d -> new ArrayList<>());
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).pos().equals(pos)) {
                list.set(i, new Entry(pos.immutable(), f, expiresAtMs));
                return;
            }
        }
        list.add(new Entry(pos.immutable(), f, expiresAtMs));
    }

    public static void remove(ResourceKey<Level> dim, BlockPos pos) {
        List<Entry> list = REG.get(dim);
        if (list == null) return;
        list.removeIf(e -> e.pos().equals(pos));
        if (list.isEmpty()) REG.remove(dim);
    }

    /** Active entry at a world position (X/Z cube), else null. */
    public static @Nullable Entry activeEntryAt(ResourceKey<Level> dim, BlockPos pos) {
        long now = System.currentTimeMillis();
        List<Entry> list = REG.get(dim);
        if (list == null || list.isEmpty()) return null;

        final int x = pos.getX();
        final int z = pos.getZ();

        for (Entry e : list) {
            if (e.expiresAtMs() <= now) continue; // inactive
            BlockPos c = e.pos();
            int dx = Math.abs(x - c.getX());
            int dz = Math.abs(z - c.getZ());
            if (dx <= RADIUS && dz <= RADIUS) return e;
        }
        return null;
    }

    /** Nearest entry (ACTIVE or INACTIVE) within a spherical radius; else null. */
    public static @Nullable Entry nearestWithinInclusive(ResourceKey<Level> dim, BlockPos pos, int radius) {
        List<Entry> list = REG.get(dim);
        if (list == null || list.isEmpty()) return null;

        final int r2 = radius * radius;
        Entry best = null;
        int bestD2 = Integer.MAX_VALUE;

        for (Entry e : list) {
            BlockPos c = e.pos();
            int dx = pos.getX() - c.getX();
            int dy = pos.getY() - c.getY();
            int dz = pos.getZ() - c.getZ();
            int d2 = dx*dx + dy*dy + dz*dz;
            if (d2 <= r2 && d2 < bestD2) { best = e; bestD2 = d2; }
        }
        return best;
    }

    /**
     * Extend expiry by deltaMs (works even if currently inactive; restarts from now).
     * @return true if the catalyst was found and updated.
     */
    public static boolean extendExpiryAllowInactive(ResourceKey<Level> dim, BlockPos catalystPos, long deltaMs) {
        return extendExpiryAllowInactiveAndGet(dim, catalystPos, deltaMs) != null;
    }

    /**
     * Same as {@link #extendExpiryAllowInactive} but returns the UPDATED entry
     * so callers can immediately read the new expiration time.
     */
    public static @Nullable Entry extendExpiryAllowInactiveAndGet(ResourceKey<Level> dim, BlockPos catalystPos, long deltaMs) {
        long now = System.currentTimeMillis();
        List<Entry> list = REG.get(dim);
        if (list == null) return null;

        for (int i = 0; i < list.size(); i++) {
            Entry e = list.get(i);
            if (!e.pos().equals(catalystPos)) continue;

            long base = Math.max(now, e.expiresAtMs());
            long newExp = base + Math.max(0L, deltaMs);
            Entry updated = new Entry(e.pos(), e.faction(), newExp);
            list.set(i, updated);
            return updated;
        }
        return null;
    }
}
