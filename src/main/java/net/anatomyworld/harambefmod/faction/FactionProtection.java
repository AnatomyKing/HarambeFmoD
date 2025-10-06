package net.anatomyworld.harambefmod.faction;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.*;

public final class FactionProtection {
    private FactionProtection() {}

    public static final int RADIUS = 32;
    private static final int R2 = RADIUS * RADIUS;

    /** If two centers are within this sq. distance, their protection areas overlap. */
    private static final int OVERLAP_R2 = (RADIUS + RADIUS) * (RADIUS + RADIUS); // 64^2

    private static final int SHIFT = 5; // 2^5 = 32 (cell size = radius)
    private static final Map<ResourceKey<Level>, Map<Long, List<Zone>>> ZONES = new HashMap<>();

    public static void track(ResourceKey<Level> dim, BlockPos pos, Faction faction) {
        ZONES
                .computeIfAbsent(dim, k -> new HashMap<>())
                .computeIfAbsent(cellKey(pos), k -> new ArrayList<>())
                .add(new Zone(pos.immutable(), faction));
    }

    public static void untrack(ResourceKey<Level> dim, BlockPos pos) {
        Map<Long, List<Zone>> grid = ZONES.get(dim);
        if (grid == null) return;
        long key = cellKey(pos);
        List<Zone> list = grid.get(key);
        if (list == null) return;
        list.removeIf(z -> z.center().equals(pos));
        if (list.isEmpty()) grid.remove(key);
    }

    /** Any zone whose center is within RADIUS horizontally of pos. */
    public static Zone findZone(ResourceKey<Level> dim, BlockPos pos) {
        Map<Long, List<Zone>> grid = ZONES.get(dim);
        if (grid == null || grid.isEmpty()) return null;

        int cx = pos.getX() >> SHIFT;
        int cz = pos.getZ() >> SHIFT;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                List<Zone> bucket = grid.get(pack(cx + dx, cz + dz));
                if (bucket == null) continue;
                for (Zone z : bucket) {
                    if (horizontalDist2(z.center(), pos) <= R2) return z;
                }
            }
        }
        return null;
    }

    /** True if placing at {@code center} would overlap ANY rival faction zone (full R+R check). */
    public static boolean wouldOverlapOtherFaction(ResourceKey<Level> dim, BlockPos center, Faction myFaction) {
        Map<Long, List<Zone>> grid = ZONES.get(dim);
        if (grid == null || grid.isEmpty()) return false;

        int cx = center.getX() >> SHIFT;
        int cz = center.getZ() >> SHIFT;

        // need up to 2 cells because overlap radius is 64 blocks
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                List<Zone> bucket = grid.get(pack(cx + dx, cz + dz));
                if (bucket == null) continue;
                for (Zone z : bucket) {
                    if (z.faction() != myFaction && horizontalDist2(z.center(), center) <= OVERLAP_R2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public record Zone(BlockPos center, Faction faction) {}

    private static long cellKey(BlockPos pos) { return pack(pos.getX() >> SHIFT, pos.getZ() >> SHIFT); }
    private static long pack(int x, int z) { return ((long) x << 32) ^ (z & 0xffffffffL); }
    private static int horizontalDist2(BlockPos a, BlockPos b) {
        int dx = a.getX() - b.getX();
        int dz = a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }
}
