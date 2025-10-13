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
 * Persistence: the authoritative "expiresAtMs" lives on each catalyst's BlockEntity and is
 * synchronized into this registry on load; feeds update both the registry AND the BE so data
 * is saved to disk and survives restarts.
 */
public final class CatalystRegistry {
    private CatalystRegistry() {}

    /** Radius in blocks (matches protection + overlay). */
    public static final int RADIUS = 32;
    public static final int RADIUS_PLUS_ONE = RADIUS + 1;

    /** Hard cap: 72h from *now*. */
    public static final long LIFETIME_MS = 72L * 60L * 60L * 1000L;

    /** Sculk-like “nearby death” radius (blocks). */
    public static final int BLOOM_RADIUS_BLOCKS = 8;

    /** Bloom bonuses (ms): passive +12s, hostile +6s. */
    public static final long BONUS_HOSTILE_MS = 6_000L;   // +6s (hostile/player)
    public static final long BONUS_PASSIVE_MS = 12_000L;  // +12s (adult animals)

    /** Immutable entry for a catalyst zone. */
    public record Entry(BlockPos pos, Faction faction, long expiresAtMs) {}

    private static final Map<ResourceKey<Level>, List<Entry>> REG = new HashMap<>();

    /** Add or replace a catalyst entry. expiresAtMs may be 0 to start inactive. */
    public static void put(ResourceKey<Level> dim, BlockPos pos, Faction f, long expiresAtMs) {
        List<Entry> list = REG.computeIfAbsent(dim, d -> new ArrayList<>());
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).pos().equals(pos)) {
                list.set(i, new Entry(pos.immutable(), f, Math.max(0L, expiresAtMs)));
                return;
            }
        }
        list.add(new Entry(pos.immutable(), f, Math.max(0L, expiresAtMs)));
    }

    public static void remove(ResourceKey<Level> dim, BlockPos pos) {
        List<Entry> list = REG.get(dim);
        if (list == null) return;
        list.removeIf(e -> e.pos().equals(pos));
        if (list.isEmpty()) REG.remove(dim);
    }

    /** Active entry whose Area contains the position (X/Z only); else null. */
    public static @Nullable Entry activeEntryAt(ResourceKey<Level> dim, BlockPos pos) {
        long now = System.currentTimeMillis();
        List<Entry> list = REG.get(dim);
        if (list == null || list.isEmpty()) return null;
        final int x = pos.getX(), z = pos.getZ();
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

    /** Return any entry whose Area overlaps the candidate Area (borders may touch without overlap). */
    public static @Nullable Entry anyOverlapping(ResourceKey<Level> dim, BlockPos candidateCenter) {
        List<Entry> list = REG.get(dim);
        if (list == null || list.isEmpty()) return null;
        for (Entry e : list) {
            if (areasOverlap(candidateCenter, e.pos(), RADIUS)) return e;
        }
        return null;
    }

    /** Return any **active** entry whose Area overlaps the candidate Area. */
    public static @Nullable Entry anyActiveOverlapping(ResourceKey<Level> dim, BlockPos candidateCenter) {
        long now = System.currentTimeMillis();
        List<Entry> list = REG.get(dim);
        if (list == null || list.isEmpty()) return null;
        for (Entry e : list) {
            if (e.expiresAtMs() > now && areasOverlap(candidateCenter, e.pos(), RADIUS)) return e;
        }
        return null;
    }

    /** Whether candidate is inside any Area of the given faction (active or inactive). */
    public static @Nullable Entry sameFactionContaining(ResourceKey<Level> dim, BlockPos candidate, Faction faction) {
        List<Entry> list = REG.get(dim);
        if (list == null || list.isEmpty()) return null;
        final int x = candidate.getX(), z = candidate.getZ();
        for (Entry e : list) {
            if (e.faction() != faction) continue;
            BlockPos c = e.pos();
            int dx = Math.abs(x - c.getX());
            int dz = Math.abs(z - c.getZ());
            if (dx <= RADIUS && dz <= RADIUS) return e;
        }
        return null;
    }

    /** Extend expiry by deltaMs and clamp to 72h-from-now; returns updated entry or null. */
    public static @Nullable Entry extendExpiryAllowInactiveAndGet(ResourceKey<Level> dim, BlockPos catalystPos, long deltaMs) {
        long now = System.currentTimeMillis();
        long cap = now + LIFETIME_MS;

        List<Entry> list = REG.get(dim);
        if (list == null) return null;

        for (int i = 0; i < list.size(); i++) {
            Entry e = list.get(i);
            if (!e.pos().equals(catalystPos)) continue;

            long base = Math.max(now, e.expiresAtMs());
            long newExp = Math.min(cap, base + Math.max(0L, deltaMs));
            Entry updated = new Entry(e.pos(), e.faction(), newExp);
            list.set(i, updated);
            return updated;
        }
        return null;
    }

    /* --------------------------- helpers --------------------------- */

    /** True if Areas (X/Z squares) overlap with volume, not just touch. Uses inclusive-min/exclusive-max intervals. */
    private static boolean areasOverlap(BlockPos a, BlockPos b, int r) {
        int aMinX = a.getX() - r, aMaxX = a.getX() + r + 1;
        int aMinZ = a.getZ() - r, aMaxZ = a.getZ() + r + 1;
        int bMinX = b.getX() - r, bMaxX = b.getX() + r + 1;
        int bMinZ = b.getZ() - r, bMaxZ = b.getZ() + r + 1;

        boolean xOverlap = aMinX < bMaxX && bMinX < aMaxX; // borders touching => false
        boolean zOverlap = aMinZ < bMaxZ && bMinZ < aMaxZ;
        return xOverlap && zOverlap;
    }
}
