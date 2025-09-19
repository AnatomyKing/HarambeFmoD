package net.anatomyworld.harambefmod.worldgen.trees;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.worldgen.TreeStyles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Mischief — mangrove x wisteria x spooky (roots up-oriented + fully connected)
 * - Trunk sits slightly above ground on arched stilt/prop roots (intentional spacing)
 * - Forked upper trunk with compact crown + long “wisteria” leaf curtains
 * - ROOTS: logs are always placed with Axis.Y (caps face sky), and the path is
 *          6-connected via a bridged 3D supercover line (no diagonal-only contacts).
 *
 * Requires TreeStyles helpers:
 *   - placeLogIfReplaceable(WorldGenLevel, BlockState, BlockPos, Direction.Axis)
 *   - placeLeavesIfReplaceable(WorldGenLevel, BlockState, BlockPos)
 *   - canReplace(WorldGenLevel, BlockPos)
 */
public final class MischiefTreeBuilder implements TreeStyles.PatchTreeBuilder {

    /* ===== Blocks ===== */
    private static BlockState LOG()    { return ModBlocks.MISCHIEF_LOG.get().defaultBlockState(); }
    private static BlockState LEAVES() { return ModBlocks.MISCHIEF_LEAVES.get().defaultBlockState(); }

    /* ===== Tunables ===== */
    // Overall
    private static final int   TRUNK_MIN = 9, TRUNK_MAX = 13;         // above the lifted base
    private static final int   ROOT_LIFT_MIN = 2, ROOT_LIFT_MAX = 3;  // how high trunk sits on stilts

    // Crown
    private static final int   CROWN_RX = 5, CROWN_RY = 3, CROWN_RZ = 5;
    private static final int   LOBE_MIN = 2, LOBE_MAX = 3;
    private static final int   LOBE_RX = 3, LOBE_RY = 2, LOBE_RZ = 3;

    // Forks (short, crooked)
    private static final int   FORK_COUNT_MIN = 2, FORK_COUNT_MAX = 3;
    private static final int   FORK_LEN_MIN = 2, FORK_LEN_MAX = 4;

    // Stilts (prop roots)
    private static final int   STILT_COUNT_MIN = 5, STILT_COUNT_MAX = 7;
    private static final int   STILT_FOOT_R_MIN = 3, STILT_FOOT_R_MAX = 5; // horizontal reach
    private static final int   STILT_THICK_BASE = 2;                        // taper 2->1
    private static final boolean STILT_COLLAR = true;                       // thin collar tying stilt anchors
    private static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0)); // ≈2.399963...

    // Wisteria-like drapes
    private static final int   DRAPE_ATTEMPTS = 64;
    private static final int   DRAPE_MIN = 3, DRAPE_MAX = 6;
    private static final float DRAPE_FORK_CHANCE = 0.50f;

    // Spooky accents
    private static final float SPIKE_CHANCE = 0.25f; // rare short spikes at crown rim

    @Override
    public boolean place(WorldGenLevel level, BlockPos origin, RandomSource rand) {
        // ---- elevated base ----
        int lift = Mth.nextInt(rand, ROOT_LIFT_MIN, ROOT_LIFT_MAX);          // 2..3
        BlockPos baseTop = origin.above(lift);                                // where the trunk begins
        int trunkH = Mth.nextInt(rand, TRUNK_MIN, TRUNK_MAX);                 // trunk height above baseTop

        // 1) Arched stilt roots that "stand" the tree (UP-ORIENTED + BRIDGED)
        makeStiltRoots(level, origin, baseTop, rand);

        // 2) Trunk up to crown
        buildCrookedTrunk(level, baseTop, trunkH, rand);
        BlockPos crownStem = baseTop.above(trunkH);

        // 3) Forks + crown
        List<BlockPos> forkTips = makeForks(level, crownStem, rand);
        BlockPos crownCenter = crownStem.above(1);
        leafBlob(level, crownCenter, CROWN_RX, CROWN_RY, CROWN_RZ, rand);
        int lobes = Mth.nextInt(rand, LOBE_MIN, LOBE_MAX);
        for (int i = 0; i < lobes; i++) {
            BlockPos c = crownCenter.offset(Mth.nextInt(rand, -3, 3), Mth.nextInt(rand, -1, 1), Mth.nextInt(rand, -3, 3));
            leafBlob(level, c, LOBE_RX, LOBE_RY, LOBE_RZ, rand);
        }

        // 4) Wisteria-style curtains
        drapeUnderMass(level, crownCenter, CROWN_RX + 1, CROWN_RY + 1, CROWN_RZ + 1, DRAPE_ATTEMPTS, rand);
        for (BlockPos tip : forkTips) {
            if (rand.nextFloat() < DRAPE_FORK_CHANCE) {
                drapeDown(level, tip.below(), Mth.nextInt(rand, DRAPE_MIN, DRAPE_MAX), rand);
            }
        }

        // 5) Occasional rim spike for silhouette
        if (rand.nextFloat() < SPIKE_CHANCE) {
            Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
            supercoverUpLineBridged(level, crownCenter, crownCenter.relative(d, 2).above(1), 1, 1); // up-oriented
        }

        return true;
    }

    @Override public String name() { return "Mischief"; }

    /* ======================= Stilts / Prop Roots ======================= */

    private void makeStiltRoots(WorldGenLevel level, BlockPos groundCenter, BlockPos baseTop, RandomSource rand) {
        int count = Mth.nextInt(rand, STILT_COUNT_MIN, STILT_COUNT_MAX);  // 5..7
        double phase = rand.nextDouble() * Math.PI * 2.0;
        int footR = Mth.nextInt(rand, STILT_FOOT_R_MIN, STILT_FOOT_R_MAX);

        // Compute anchor ring + ground feet
        List<BlockPos> anchors = new ArrayList<>(count);
        List<BlockPos> feet = new ArrayList<>(count);
        Set<BlockPos> used = new HashSet<>();

        for (int i = 0; i < count; i++) {
            double a = phase + i * GOLDEN_ANGLE + (rand.nextDouble() - 0.5) * 0.18; // tiny jitter
            int ax = baseTop.getX() + (int)Math.round(Math.cos(a) * 1.5);
            int az = baseTop.getZ() + (int)Math.round(Math.sin(a) * 1.5);
            BlockPos anchor = new BlockPos(ax, baseTop.getY(), az);
            if (!used.add(anchor)) continue;
            anchors.add(anchor);

            int fx = groundCenter.getX() + (int)Math.round(Math.cos(a) * footR);
            int fz = groundCenter.getZ() + (int)Math.round(Math.sin(a) * footR);
            BlockPos foot = new BlockPos(fx, groundCenter.getY(), fz);
            feet.add(foot);
        }
        if (anchors.isEmpty()) return;

        // Thin collar tying some neighbors — use UP oriented logs + bridged path
        if (STILT_COLLAR && anchors.size() >= 2) {
            for (int i = 0; i < anchors.size(); i++) {
                BlockPos a = anchors.get(i);
                BlockPos b = anchors.get((i + 1) % anchors.size());
                if (rand.nextBoolean()) supercoverUpLineBridged(level, a, b, 1, 1);
            }
        }

        // Arched stilts (two segments) — all UP oriented + bridged
        for (int i = 0; i < anchors.size(); i++) {
            BlockPos start = anchors.get(i);
            BlockPos foot  = feet.get(i);

            BlockPos mid = new BlockPos(
                    (start.getX() + foot.getX()) / 2,
                    start.getY() - Mth.nextInt(rand, 1, 2),   // soft arch
                    (start.getZ() + foot.getZ()) / 2
            );

            supercoverUpLineBridged(level, start, mid, STILT_THICK_BASE, 1);
            supercoverUpLineBridged(level, mid,   foot,  1, 1);

            // peg foot down
            pegDownUp(level, foot);
        }

        // Fuse baseTop to trunk center (UP oriented)
        supercoverUpLineBridged(level,
                groundCenter.above(baseTop.getY() - groundCenter.getY()),
                baseTop, 1, 1);
    }

    private void pegDownUp(WorldGenLevel level, BlockPos p) {
        if (TreeStyles.canReplace(level, p.below()))
            TreeStyles.placeLogIfReplaceable(level, LOG(), p.below(), Direction.Axis.Y);
        if (TreeStyles.canReplace(level, p.below(2)))
            TreeStyles.placeLogIfReplaceable(level, LOG(), p.below(2), Direction.Axis.Y);
    }

    /* ======================= Trunk & Forks ======================= */

    private void buildCrookedTrunk(WorldGenLevel level, BlockPos baseTop, int height, RandomSource rand) {
        BlockPos p = baseTop;
        for (int y = 0; y < height; y++) {
            TreeStyles.placeLogIfReplaceable(level, LOG(), p, Direction.Axis.Y);
            if (y > 0 && y % 3 == 0 && rand.nextFloat() < 0.5f) {
                Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
                BlockPos side = p.relative(d);
                TreeStyles.placeLogIfReplaceable(level, LOG(), side, d.getAxis());
                p = side.above();
            } else {
                p = p.above();
            }
            if (rand.nextFloat() < 0.18f) {
                Direction kd = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
                TreeStyles.placeLogIfReplaceable(level, LOG(), p.relative(kd), kd.getAxis());
            }
        }
        TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), p);
    }

    private List<BlockPos> makeForks(WorldGenLevel level, BlockPos top, RandomSource rand) {
        int forks = Mth.nextInt(rand, FORK_COUNT_MIN, FORK_COUNT_MAX);
        List<BlockPos> tips = new ArrayList<>(forks);
        double baseAngle = rand.nextDouble() * Math.PI * 2.0;

        for (int i = 0; i < forks; i++) {
            double a = baseAngle + i * (2 * Math.PI / forks) + (rand.nextDouble() - 0.5) * 0.3;
            double dx = Math.cos(a), dz = Math.sin(a);
            int len = Mth.nextInt(rand, FORK_LEN_MIN, FORK_LEN_MAX);

            BlockPos start = top;
            BlockPos mid   = start.offset((int)Math.round(dx * (len * 0.6)), 1 + rand.nextInt(2), (int)Math.round(dz * (len * 0.6)));
            BlockPos tip   = start.offset((int)Math.round(dx *  len),        1 + rand.nextInt(2), (int)Math.round(dz *  len));

            // forks can remain standard (axis by dominant)
            supercoverLineLogs(level, start, mid);
            supercoverLineLogs(level, mid,   tip);

            leafRibbon(level, start, tip, rand);
            tips.add(tip);
        }
        return tips;
    }

    /* ======================= Crown & Drapes ======================= */

    private void leafBlob(WorldGenLevel level, BlockPos c, int rx, int ry, int rz, RandomSource rand) {
        for (int dx = -rx; dx <= rx; dx++)
            for (int dy = -ry; dy <= ry; dy++)
                for (int dz = -rz; dz <= rz; dz++) {
                    double nx = dx / (double)Math.max(1, rx);
                    double ny = dy / (double)Math.max(1, ry);
                    double nz = dz / (double)Math.max(1, rz);
                    double d = nx*nx + ny*ny + nz*nz;
                    if (d <= 1.02 + (rand.nextFloat()*0.05f - 0.025f)) {
                        TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), c.offset(dx, dy, dz));
                        if (rand.nextFloat() < 0.04f)
                            TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), c.offset(dx, dy - 1, dz));
                    }
                }
    }

    private void leafRibbon(WorldGenLevel level, BlockPos from, BlockPos to, RandomSource rand) {
        int steps = Math.max(Math.max(Math.abs(to.getX()-from.getX()), Math.abs(to.getY()-from.getY())), Math.abs(to.getZ()-from.getZ()));
        if (steps <= 0) return;
        double sx = from.getX(), sy = from.getY(), sz = from.getZ();
        double dx = (to.getX()-from.getX())/(double)steps;
        double dy = (to.getY()-from.getY())/(double)steps;
        double dz = (to.getZ()-from.getZ())/(double)steps;
        for (int i = 0; i <= steps; i++) {
            BlockPos p = new BlockPos(Mth.floor(sx), Mth.floor(sy), Mth.floor(sz));
            TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), p);
            if (rand.nextBoolean()) TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), p.above());
            sx += dx; sy += dy; sz += dz;
        }
    }

    private void drapeUnderMass(WorldGenLevel level, BlockPos center, int rx, int ry, int rz, int attempts, RandomSource rand) {
        for (int i = 0; i < attempts; i++) {
            int ox = Mth.nextInt(rand, -rx, rx);
            int oz = Mth.nextInt(rand, -rz, rz);
            for (int y = center.getY() + ry; y >= center.getY() - ry; y--) {
                BlockPos p = new BlockPos(center.getX() + ox, y, center.getZ() + oz);
                BlockPos below = p.below();
                if (level.getBlockState(p).getBlock() == LEAVES().getBlock() && TreeStyles.canReplace(level, below)) {
                    drapeDown(level, below, Mth.nextInt(rand, DRAPE_MIN, DRAPE_MAX), rand);
                    break;
                }
            }
        }
    }

    private void drapeDown(WorldGenLevel level, BlockPos start, int length, RandomSource rand) {
        BlockPos p = start;
        for (int i = 0; i < length; i++) {
            if (!TreeStyles.canReplace(level, p)) break;
            TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), p);
            p = p.below();
            if (i > 0 && rand.nextFloat() < 0.12f) p = p.below(); // occasional gap
        }
        if (rand.nextFloat() < 0.3f && TreeStyles.canReplace(level, p)) {
            TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), p);
        }
    }

    /* ======================= Supercover lines ======================= */
    // Standard (axis based on motion) — used for forks/spikes only
    private void supercoverLineLogs(WorldGenLevel level, BlockPos a, BlockPos b) {
        step3D(level, a, b, false, 1, 1);
    }

    // UP-oriented + bridged — used for ALL root/collar/fuse segments
    private void supercoverUpLineBridged(WorldGenLevel level, BlockPos a, BlockPos b, int startThick, int endThick) {
        step3D(level, a, b, true, startThick, endThick);
    }

    /**
     * 3D line traversal with supercover-style bridging:
     * - If two minor axes advance in the same iteration, place intermediate voxels
     *   so every step is face-adjacent (no corner-only contacts).
     * - If upOriented=true, all placed logs use Axis.Y; otherwise axis follows motion.
     */
    private void step3D(WorldGenLevel level, BlockPos a, BlockPos b, boolean upOriented, int startThick, int endThick) {
        int x0=a.getX(), y0=a.getY(), z0=a.getZ();
        int x1=b.getX(), y1=b.getY(), z1=b.getZ();

        int dx=Math.abs(x1-x0), dy=Math.abs(y1-y0), dz=Math.abs(z1-z0);
        int sx=Integer.signum(x1-x0), sy=Integer.signum(y1-y0), sz=Integer.signum(z1-z0);

        int px=x0, py=y0, pz=z0;
        int nSteps = Math.max(dx, Math.max(dy, dz));
        if (nSteps == 0) {
            placeUpThick(level, new BlockPos(px,py,pz), upOriented ? Direction.Axis.Y : Direction.Axis.Y, startThick);
            return;
        }

        // Choose dominant axis branch
        if (dx >= dy && dx >= dz) {
            int e1 = 2*dy - dx, e2 = 2*dz - dx;
            for (int i=0; i<=dx; i++) {
                int thick = lerpInt(i, startThick, endThick, dx);
                placeUpThick(level, new BlockPos(px,py,pz), upOriented ? Direction.Axis.Y : dominantAxis(px,py,pz, px-sx,py,pz), thick);

                boolean yStep = e1 >= 0;
                boolean zStep = e2 >= 0;

                // BRIDGE: if both minor axes step this tick, lay intermediates
                if (yStep && zStep) {
                    placeUpThick(level, new BlockPos(px, py+sy, pz), Direction.Axis.Y, thick);
                    placeUpThick(level, new BlockPos(px, py+sy, pz+sz), Direction.Axis.Y, thick);
                } else if (yStep) {
                    placeUpThick(level, new BlockPos(px, py+sy, pz), Direction.Axis.Y, thick);
                } else if (zStep) {
                    placeUpThick(level, new BlockPos(px, py, pz+sz), Direction.Axis.Y, thick);
                }

                if (yStep) { py += sy; e1 -= 2*dx; }
                if (zStep) { pz += sz; e2 -= 2*dx; }

                e1 += 2*dy; e2 += 2*dz; px += sx;
            }
        } else if (dy >= dx && dy >= dz) {
            int e1 = 2*dx - dy, e2 = 2*dz - dy;
            for (int i=0; i<=dy; i++) {
                int thick = lerpInt(i, startThick, endThick, dy);
                placeUpThick(level, new BlockPos(px,py,pz), upOriented ? Direction.Axis.Y : dominantAxis(px,py,pz, px,py-sy,pz), thick);

                boolean xStep = e1 >= 0;
                boolean zStep = e2 >= 0;

                if (xStep && zStep) {
                    placeUpThick(level, new BlockPos(px+sx, py, pz), Direction.Axis.Y, thick);
                    placeUpThick(level, new BlockPos(px+sx, py, pz+sz), Direction.Axis.Y, thick);
                } else if (xStep) {
                    placeUpThick(level, new BlockPos(px+sx, py, pz), Direction.Axis.Y, thick);
                } else if (zStep) {
                    placeUpThick(level, new BlockPos(px, py, pz+sz), Direction.Axis.Y, thick);
                }

                if (xStep) { px += sx; e1 -= 2*dy; }
                if (zStep) { pz += sz; e2 -= 2*dy; }

                e1 += 2*dx; e2 += 2*dz; py += sy;
            }
        } else {
            int e1 = 2*dy - dz, e2 = 2*dx - dz;
            for (int i=0; i<=dz; i++) {
                int thick = lerpInt(i, startThick, endThick, dz);
                placeUpThick(level, new BlockPos(px,py,pz), upOriented ? Direction.Axis.Y : dominantAxis(px,py,pz, px,py,pz-sz), thick);

                boolean yStep = e1 >= 0;
                boolean xStep = e2 >= 0;

                if (yStep && xStep) {
                    placeUpThick(level, new BlockPos(px, py+sy, pz), Direction.Axis.Y, thick);
                    placeUpThick(level, new BlockPos(px+sx, py+sy, pz), Direction.Axis.Y, thick);
                } else if (yStep) {
                    placeUpThick(level, new BlockPos(px, py+sy, pz), Direction.Axis.Y, thick);
                } else if (xStep) {
                    placeUpThick(level, new BlockPos(px+sx, py, pz), Direction.Axis.Y, thick);
                }

                if (yStep) { py += sy; e1 -= 2*dz; }
                if (xStep) { px += sx; e2 -= 2*dz; }

                e1 += 2*dy; e2 += 2*dx; pz += sz;
            }
        }
    }

    private Direction.Axis dominantAxis(int x, int y, int z, int xPrev, int yPrev, int zPrev) {
        int ax = Math.abs(x - xPrev), ay = Math.abs(y - yPrev), az = Math.abs(z - zPrev);
        if (ay >= ax && ay >= az) return Direction.Axis.Y;
        return (ax >= az) ? Direction.Axis.X : Direction.Axis.Z;
    }

    /** Place a “thick” voxel where *all* logs use Axis.Y (caps up). */
    private void placeUpThick(WorldGenLevel level, BlockPos p, Direction.Axis ignored, int thick) {
        TreeStyles.placeLogIfReplaceable(level, LOG(), p, Direction.Axis.Y);
        if (thick <= 1) return;
        // expand in horizontal plane, but keep logs Axis.Y so caps still face up
        TreeStyles.placeLogIfReplaceable(level, LOG(), p.north(), Direction.Axis.Y);
        TreeStyles.placeLogIfReplaceable(level, LOG(), p.south(), Direction.Axis.Y);
        if (thick >= 2) {
            TreeStyles.placeLogIfReplaceable(level, LOG(), p.west(), Direction.Axis.Y);
            TreeStyles.placeLogIfReplaceable(level, LOG(), p.east(), Direction.Axis.Y);
        }
        if (thick >= 3) {
            TreeStyles.placeLogIfReplaceable(level, LOG(), p.above(), Direction.Axis.Y);
        }
    }

    /* ======================= Utility ======================= */

    private static int lerpInt(int i, int a, int b, int n) {
        if (n <= 0) return a;
        return a + (int)Math.round((b - a) * (i / (double)n));
    }
}
