package net.anatomyworld.harambefmod.worldgen.trees;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.worldgen.TreeStyles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Belmont (shelf-crown variant, vanilla-friendly)
 * - Mild S-curved trunk
 * - Horizontal branch arms with flat plate-like leaf disks (top-heavy silhouette)
 * - Subtle leaf drapes under plates (always-continuous chains)
 * - Terrain-following roots that step down slopes (no floaters)
 * - No orphan top-cap leaf (removed + cleanup)
 *
 * Uses ModBlocks.BELMONT_LOG & ModBlocks.BELMONT_LEAVES
 * Requires TreeStyles helpers (placeLogIfReplaceable / placeLeavesIfReplaceable / canReplace).
 */
public final class BelmontTreeBuilder implements TreeStyles.PatchTreeBuilder {

    /* ===== Tunables ===== */
    // Trunk
    private static final int TRUNK_MIN_H = 12, TRUNK_MAX_H = 17;
    private static final float TRUNK_SWAY_PROB = 0.35f;
    private static final int   TRUNK_SWAY_STEP = 3;

    // Plates (leaf shelves)
    private static final int   PLATE_MIN = 4, PLATE_MAX = 6;
    private static final int   PLATE_R_BASE_MIN = 3, PLATE_R_BASE_MAX = 5;
    private static final int   PLATE_R_TOP_BONUS = 2;
    private static final int   PLATE_THICK = 1;
    private static final float PLATE_RIM_LIP = 0.20f;
    private static final int   ARM_MIN = 2, ARM_MAX = 4;
    private static final int   ARM_THICK = 2;

    // Plate layout
    private static final float TOP_HEAVY = 0.58f;
    private static final float PLATE_VERTICAL_JITTER = 0.25f;

    // Leaf drapes (connected)
    private static final int   DRAPE_TRIES_PER_PLATE = 16;
    private static final int   DRAPE_MIN = 2, DRAPE_MAX = 4;

    // Roots (terrain-following)
    private static final int ROOTS = 3;
    private static final int ROOT_LEN_MIN = 3, ROOT_LEN_MAX = 6;
    private static final int ROOT_SURF_SEARCH_DOWN = 4;

    private static BlockState LOG()    { return ModBlocks.BELMONT_LOG.get().defaultBlockState(); }
    private static BlockState LEAVES() { return ModBlocks.BELMONT_LEAVES.get().defaultBlockState(); }

    @Override
    public boolean place(WorldGenLevel level, BlockPos origin, RandomSource rand) {
        // 1) Trunk
        int trunkH = Mth.nextInt(rand, TRUNK_MIN_H, TRUNK_MAX_H);
        List<BlockPos> spine = buildSwayTrunk(level, origin, trunkH, rand);

        // 2) Plate heights
        int plateCount = Mth.nextInt(rand, PLATE_MIN, PLATE_MAX);
        int startY = origin.getY() + Mth.floor(trunkH * (1f - TOP_HEAVY));
        int topY   = origin.getY() + trunkH - 1;
        List<Integer> plateYs = spreadHeights(startY, topY, plateCount, rand);

        // 3) Arms + plates + drapes
        for (int i = 0; i < plateYs.size(); i++) {
            int y = plateYs.get(i);

            BlockPos anchor = nearestSpine(spine, y);
            if (anchor == null) continue;

            double angle = (i * (Math.PI * (3.0 - Math.sqrt(5.0)))) + rand.nextDouble() * 0.25;
            int armLen = Mth.nextInt(rand, ARM_MIN, ARM_MAX);
            BlockPos plateCenter = anchor.offset((int)Math.round(Math.cos(angle) * armLen),
                    Mth.nextInt(rand, 0, 1),
                    (int)Math.round(Math.sin(angle) * armLen));

            supercoverLineLogsThick(level, anchor, plateCenter, ARM_THICK, 1);

            int baseR = Mth.nextInt(rand, PLATE_R_BASE_MIN, PLATE_R_BASE_MAX);
            int bonus = (i >= plateCount / 2) ? PLATE_R_TOP_BONUS : 0;
            int r = baseR + bonus;

            leafPlate(level, plateCenter, r, PLATE_THICK, rand);
            if (rand.nextFloat() < PLATE_RIM_LIP) leafRimLip(level, plateCenter, r + 1, rand);
            drapeUnderPlate(level, plateCenter, r, DRAPE_TRIES_PER_PLATE, rand);
        }

        // 4) Roots
        makeTerrainRoots(level, origin, rand);

        // 5) Cleanup: if there happens to be a single orphan leaf above the trunk tip, remove it.
        BlockPos trunkTop = spine.get(spine.size() - 1).above(); // position where a cap would be
        cleanupOrphanTopLeaf(level, trunkTop);

        // (NOTE: previously we placed a leaf cap here; that line is intentionally removed.)

        return true;
    }

    @Override public String name() { return "Belmont"; }

    /* ====================== Trunk ====================== */

    private List<BlockPos> buildSwayTrunk(WorldGenLevel level, BlockPos base, int h, RandomSource rand) {
        List<BlockPos> spine = new ArrayList<>(h);
        BlockPos p = base;
        int stepsSinceSway = 0;

        for (int y = 0; y < h; y++) {
            if (y < h / 3) {
                place2x2(level, p);
            } else {
                TreeStyles.placeLogIfReplaceable(level, LOG(), p, Direction.Axis.Y);
            }

            spine.add(p);

            stepsSinceSway++;
            if (stepsSinceSway >= TRUNK_SWAY_STEP && rand.nextFloat() < TRUNK_SWAY_PROB) {
                stepsSinceSway = 0;
                Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
                p = p.relative(d);
                TreeStyles.placeLogIfReplaceable(level, LOG(), p.relative(d.getOpposite()), d.getAxis());
            }

            p = p.above();
        }
        return spine;
    }

    private void place2x2(WorldGenLevel level, BlockPos base) {
        TreeStyles.placeLogIfReplaceable(level, LOG(), base, Direction.Axis.Y);
        TreeStyles.placeLogIfReplaceable(level, LOG(), base.east(), Direction.Axis.Y);
        TreeStyles.placeLogIfReplaceable(level, LOG(), base.south(), Direction.Axis.Y);
        TreeStyles.placeLogIfReplaceable(level, LOG(), base.east().south(), Direction.Axis.Y);
    }

    private BlockPos nearestSpine(List<BlockPos> spine, int yTarget) {
        BlockPos best = null;
        int bestDy = Integer.MAX_VALUE;
        for (BlockPos s : spine) {
            int dy = Math.abs(s.getY() - yTarget);
            if (dy < bestDy) { bestDy = dy; best = s; }
        }
        return best;
    }

    private List<Integer> spreadHeights(int yStart, int yEnd, int count, RandomSource rand) {
        List<Integer> out = new ArrayList<>(count);
        if (yEnd <= yStart) { out.add(yStart); return out; }
        double step = (yEnd - yStart) / (double)Math.max(1, count - 1);
        for (int i = 0; i < count; i++) {
            int y = Mth.floor(yStart + step * i + (rand.nextDouble() - 0.5) * PLATE_VERTICAL_JITTER * 2.0);
            out.add(y);
        }
        return out;
    }

    /* ====================== Plates (leaf shelves) ====================== */

    private void leafPlate(WorldGenLevel level, BlockPos center, int r, int thickness, RandomSource rand) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz <= r * r) {
                    BlockPos p = center.offset(dx, 0, dz);
                    TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), p);
                    if (thickness >= 2 && dx*dx + dz*dz <= (r - 1)*(r - 1) && rand.nextFloat() < 0.35f) {
                        TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), p.below());
                    }
                }
            }
        }
    }

    private void leafRimLip(WorldGenLevel level, BlockPos center, int r, RandomSource rand) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int d2 = dx*dx + dz*dz;
                if (d2 <= r*r && d2 >= (r-1)*(r-1) && rand.nextFloat() < 0.55f) {
                    TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), center.offset(dx, 0, dz));
                }
            }
        }
    }

    private void drapeUnderPlate(WorldGenLevel level, BlockPos center, int r, int attempts, RandomSource rand) {
        for (int i = 0; i < attempts; i++) {
            int dx = Mth.nextInt(rand, -r, r);
            int dz = Mth.nextInt(rand, -r, r);
            if (dx*dx + dz*dz > r*r) continue;

            BlockPos start = center.offset(dx, -1, dz);
            if (!TreeStyles.canReplace(level, start)) continue;

            int len = Mth.nextInt(rand, DRAPE_MIN, DRAPE_MAX);
            drapeDownConnected(level, start, len);
        }
    }

    /** Always places a continuous vertical leaf chain of at least 2 blocks (no orphan bottom leaves). */
    private void drapeDownConnected(WorldGenLevel level, BlockPos start, int length) {
        if (length < 2) length = 2;
        BlockPos p = start;
        int placed = 0;

        for (int i = 0; i < length; i++) {
            if (!TreeStyles.canReplace(level, p)) break;
            TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), p);
            placed++;
            p = p.below();
        }

        if (placed == 1 && TreeStyles.canReplace(level, start.below())) {
            TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), start.below());
        }
    }

    /* ====================== Roots (terrain-following) ====================== */

    private void makeTerrainRoots(WorldGenLevel level, BlockPos base, RandomSource rand) {
        Direction[] dirs = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };
        List<Direction> choices = new ArrayList<>(List.of(dirs));
        java.util.Collections.shuffle(choices, new java.util.Random(rand.nextLong()));

        for (int i = 0; i < ROOTS; i++) {
            Direction d = choices.get(i);
            int len = Mth.nextInt(rand, ROOT_LEN_MIN, ROOT_LEN_MAX);

            BlockPos curr = snapToSurface(level, base.relative(d));
            BlockPos prev = curr;
            int thick = 2;

            for (int step = 1; step <= len; step++) {
                BlockPos next = snapToSurface(level, curr.relative(d));
                supercoverLineLogsThick(level, curr, next, thick, Math.max(1, thick - 1));
                curr = next;
                prev = curr;
                if (step >= len / 2) thick = 1;
            }

            BlockPos below = prev.below();
            if (TreeStyles.canReplace(level, below)) {
                TreeStyles.placeLogIfReplaceable(level, LOG(), below, Direction.Axis.Y);
            }
        }
    }

    private BlockPos snapToSurface(WorldGenLevel level, BlockPos guess) {
        BlockPos best = guess;
        for (int dy = 0; dy <= ROOT_SURF_SEARCH_DOWN; dy++) {
            BlockPos here  = guess.below(dy);
            BlockPos below = here.below();
            if (!TreeStyles.canReplace(level, below)) {
                if (TreeStyles.canReplace(level, here)) best = here;
                else best = here.above();
                return best;
            }
        }
        if (!TreeStyles.canReplace(level, best)) best = best.above();
        return best;
    }

    /* ====================== Leaf orphan cleanup ====================== */

    /** Remove a lone leaf at the trunk tip if it has fewer than 2 neighboring leaves (prevents single top leaf). */
    private void cleanupOrphanTopLeaf(WorldGenLevel level, BlockPos pos) {
        BlockState st = level.getBlockState(pos);
        if (st.getBlock() != LEAVES().getBlock()) return;

        int neighbors = 0;
        for (Direction d : Direction.values()) {
            if (level.getBlockState(pos.relative(d)).getBlock() == LEAVES().getBlock()) {
                neighbors++;
                if (neighbors >= 2) return; // not orphaned
            }
        }
        // orphan => remove it
        level.removeBlock(pos, false);
    }

    /* ====================== Connected line with taper ====================== */

    private void supercoverLineLogsThick(WorldGenLevel level, BlockPos a, BlockPos b, int startThick, int endThick) {
        int x0 = a.getX(), y0 = a.getY(), z0 = a.getZ();
        int x1 = b.getX(), y1 = b.getY(), z1 = b.getZ();
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0), dz = Math.abs(z1 - z0);
        int sx = Integer.signum(x1 - x0), sy = Integer.signum(y1 - y0), sz = Integer.signum(z1 - z0);
        int px = x0, py = y0, pz = z0;

        if (dx >= dy && dx >= dz) {
            int p1 = 2*dy - dx, p2 = 2*dz - dx;
            for (int i = 0; i <= dx; i++) {
                placeThickVoxel(level, new BlockPos(px,py,pz), dominantAxis(px,py,pz, x0,y0,z0), lerpInt(i, startThick, endThick, dx));
                if (p1 >= 0) { py += sy; p1 -= 2*dx; }
                if (p2 >= 0) { pz += sz; p2 -= 2*dx; }
                p1 += 2*dy; p2 += 2*dz; px += sx;
            }
        } else if (dy >= dx && dy >= dz) {
            int p1 = 2*dx - dy, p2 = 2*dz - dy;
            for (int i = 0; i <= dy; i++) {
                placeThickVoxel(level, new BlockPos(px,py,pz), dominantAxis(px,py,pz, x0,y0,z0), lerpInt(i, startThick, endThick, dy));
                if (p1 >= 0) { px += sx; p1 -= 2*dy; }
                if (p2 >= 0) { pz += sz; p2 -= 2*dy; }
                p1 += 2*dx; p2 += 2*dz; py += sy;
            }
        } else {
            int p1 = 2*dy - dz, p2 = 2*dx - dz;
            for (int i = 0; i <= dz; i++) {
                placeThickVoxel(level, new BlockPos(px,py,pz), dominantAxis(px,py,pz, x0,y0,z0), lerpInt(i, startThick, endThick, dz));
                if (p1 >= 0) { py += sy; p1 -= 2*dz; }
                if (p2 >= 0) { px += sx; p2 -= 2*dz; }
                p1 += 2*dy; p2 += 2*dx; pz += sz;
            }
        }
    }

    private Direction.Axis dominantAxis(int x, int y, int z, int x0, int y0, int z0) {
        int ax = Math.abs(x - x0), ay = Math.abs(y - y0), az = Math.abs(z - z0);
        if (ay >= ax && ay >= az) return Direction.Axis.Y;
        return (ax >= az) ? Direction.Axis.X : Direction.Axis.Z;
    }

    private void placeThickVoxel(WorldGenLevel level, BlockPos p, Direction.Axis axis, int thick) {
        TreeStyles.placeLogIfReplaceable(level, LOG(), p, axis);
        if (thick <= 1) return;
        if (axis == Direction.Axis.X) {
            TreeStyles.placeLogIfReplaceable(level, LOG(), p.north(), Direction.Axis.Z);
            TreeStyles.placeLogIfReplaceable(level, LOG(), p.south(), Direction.Axis.Z);
        } else if (axis == Direction.Axis.Z) {
            TreeStyles.placeLogIfReplaceable(level, LOG(), p.west(), Direction.Axis.X);
            TreeStyles.placeLogIfReplaceable(level, LOG(), p.east(), Direction.Axis.X);
        } else {
            TreeStyles.placeLogIfReplaceable(level, LOG(), p.west(), Direction.Axis.X);
            TreeStyles.placeLogIfReplaceable(level, LOG(), p.east(), Direction.Axis.X);
            TreeStyles.placeLogIfReplaceable(level, LOG(), p.north(), Direction.Axis.Z);
            TreeStyles.placeLogIfReplaceable(level, LOG(), p.south(), Direction.Axis.Z);
        }
    }

    private static int lerpInt(int i, int a, int b, int n) {
        if (n <= 0) return a;
        return a + (int)Math.round((b - a) * (i / (double)n));
    }
}
