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
 * DYNASTY: heavy 2×2 braided trunk + unified crown.
 * Branches are laid with a supercover 3D line so logs remain face-connected.
 */
public final class DynastyTreeBuilder implements TreeStyles.PatchTreeBuilder {

    // Tunables
    private static final int MIN_TRUNK = 10, MAX_TRUNK = 14;      // trunk height
    private static final int BRANCHES_MIN = 4, BRANCHES_MAX = 6;  // branch count

    private static BlockState DYNASTY_LOG()    { return ModBlocks.DYNASTY_LOG.get().defaultBlockState(); }
    private static BlockState DYNASTY_LEAVES() { return ModBlocks.DYNASTY_LEAVES.get().defaultBlockState(); }

    @Override
    public boolean place(WorldGenLevel level, BlockPos origin, RandomSource rand) {
        // === 1) 2×2 braided trunk with a small buttress base ===
        int trunkH = Mth.nextInt(rand, MIN_TRUNK, MAX_TRUNK);
        int cx = origin.getX(), cz = origin.getZ();

        // base flare
        for (int y = 0; y < Math.min(3, trunkH); y++) {
            plus3x3(level, new BlockPos(cx, origin.getY() + y, cz));
        }

        // build upward with a gentle swirl and collect "collars" (branch anchors)
        Direction[] swirl = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
        int swirlIdx = rand.nextInt(4);
        List<BlockPos> collars = new ArrayList<>();

        for (int y = 3; y < trunkH; y++) {
            BlockPos base = new BlockPos(cx, origin.getY() + y, cz);
            twoByTwo(level, base);

            if (y >= trunkH / 2 && y <= trunkH - 2) {
                collars.add(base);
                collars.add(base.offset(1, 0, 0));
                collars.add(base.offset(0, 0, 1));
                collars.add(base.offset(1, 0, 1));
            }

            if (y % 2 == 0) {
                Direction d = swirl[swirlIdx];
                cx += d.getStepX();
                cz += d.getStepZ();
                swirlIdx = (swirlIdx + (rand.nextBoolean() ? 1 : 3)) & 3;
            }
        }

        BlockPos crownCenter = new BlockPos(cx, origin.getY() + trunkH + 1, cz);

        // === 2) Build a single, unified central crown first ===
        leafBlob(level, crownCenter, 5, 3, 5, rand);
        leafBlob(level, crownCenter.above(1), 4, 2, 4, rand);

        // === 3) Curved, connected, thicker branches merging into the crown ===
        int branchCount = Mth.nextInt(rand, BRANCHES_MIN, BRANCHES_MAX);
        for (int i = 0; i < branchCount && !collars.isEmpty(); i++) {
            BlockPos start = collars.remove(rand.nextInt(collars.size())).above();

            Direction dirH = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
            double dx = dirH.getStepX(), dz = dirH.getStepZ();
            double rise = 0.35 + rand.nextDouble() * 0.45;
            int len = Mth.nextInt(rand, 7, 10);

            // 3-point polyline (gentle curve)
            BlockPos mid1 = start.offset((int)Math.round(dx * (len * 0.35)), (int)Math.round(rise * (len * 0.45)), (int)Math.round(dz * (len * 0.35)));
            BlockPos mid2 = start.offset((int)Math.round(dx * (len * 0.70)), (int)Math.round(rise * (len * 0.85)), (int)Math.round(dz * (len * 0.70)));
            BlockPos tip  = start.offset((int)Math.round(dx *  len       ), (int)Math.round(rise *  len       ), (int)Math.round(dz *  len       ));

            // Lay continuous logs along each segment (no diagonal gaps)
            supercoverLineLogs(level, start, mid1, rand, true);
            supercoverLineLogs(level, mid1,  mid2, rand, false);
            supercoverLineLogs(level, mid2,  tip,  rand, false);

            // Tip canopy that overlaps the main crown
            leafBlob(level, tip, 3, 2, 3, rand);
            leafRibbon(level, mid1, tip, rand);

            // Hidden support vein into the crown (helps silhouette cohesion)
            if (rand.nextFloat() < 0.7f) supercoverLineLogs(level, tip, crownCenter, rand, false);
        }

        // === 4) Buttress roots for weight and grounding ===
        makeRoots(level, origin, rand);

        return true;
    }

    @Override
    public String name() { return "Dynasty"; }

    /* ---------- trunk & base ---------- */

    private void twoByTwo(WorldGenLevel level, BlockPos base) {
        TreeStyles.placeLogIfReplaceable(level, DYNASTY_LOG(), base,                     Direction.Axis.Y);
        TreeStyles.placeLogIfReplaceable(level, DYNASTY_LOG(), base.offset(1,0,0),       Direction.Axis.Y);
        TreeStyles.placeLogIfReplaceable(level, DYNASTY_LOG(), base.offset(0,0,1),       Direction.Axis.Y);
        TreeStyles.placeLogIfReplaceable(level, DYNASTY_LOG(), base.offset(1,0,1),       Direction.Axis.Y);
    }

    private void plus3x3(WorldGenLevel level, BlockPos c) {
        twoByTwo(level, c);
        TreeStyles.placeLogIfReplaceable(level, DYNASTY_LOG(), c.west(),                 Direction.Axis.X);
        TreeStyles.placeLogIfReplaceable(level, DYNASTY_LOG(), c.east().east(),          Direction.Axis.X);
        TreeStyles.placeLogIfReplaceable(level, DYNASTY_LOG(), c.north(),                Direction.Axis.Z);
        TreeStyles.placeLogIfReplaceable(level, DYNASTY_LOG(), c.south().south(),        Direction.Axis.Z);
    }

    /* ---------- canopy ---------- */

    private void leafBlob(WorldGenLevel level, BlockPos center, int rx, int ry, int rz, RandomSource rand) {
        for (int dx = -rx; dx <= rx; dx++) {
            for (int dy = -ry; dy <= ry; dy++) {
                for (int dz = -rz; dz <= rz; dz++) {
                    double nx = dx / (double)Math.max(1, rx);
                    double ny = dy / (double)Math.max(1, ry);
                    double nz = dz / (double)Math.max(1, rz);
                    double d = nx*nx + ny*ny + nz*nz;
                    if (d <= 1.03 + (rand.nextFloat()*0.06f - 0.03f)) {
                        TreeStyles.placeLeavesIfReplaceable(level, DYNASTY_LEAVES(), center.offset(dx, dy, dz));
                        if (rand.nextFloat() < 0.04f)
                            TreeStyles.placeLeavesIfReplaceable(level, DYNASTY_LEAVES(), center.offset(dx, dy-1, dz));
                    }
                }
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
            TreeStyles.placeLeavesIfReplaceable(level, DYNASTY_LEAVES(), p);
            if (rand.nextBoolean()) TreeStyles.placeLeavesIfReplaceable(level, DYNASTY_LEAVES(), p.above());
            if (rand.nextBoolean()) {
                Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
                TreeStyles.placeLeavesIfReplaceable(level, DYNASTY_LEAVES(), p.relative(d));
            }
            sx += dx; sy += dy; sz += dz;
        }
    }

    /* ---------- connected-branch raster (3D supercover) ---------- */

    private void supercoverLineLogs(WorldGenLevel level, BlockPos a, BlockPos b, RandomSource rand, boolean thickenAtStart) {
        int x0 = a.getX(), y0 = a.getY(), z0 = a.getZ();
        int x1 = b.getX(), y1 = b.getY(), z1 = b.getZ();

        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0), dz = Math.abs(z1 - z0);
        int sx = Integer.signum(x1 - x0), sy = Integer.signum(y1 - y0), sz = Integer.signum(z1 - z0);

        int px = x0, py = y0, pz = z0;

        if (dx >= dy && dx >= dz) {
            int p1 = 2*dy - dx, p2 = 2*dz - dx;
            for (int i = 0; i <= dx; i++) {
                placeLogStep(level, px, py, pz, x0, y0, z0);
                if (thickenAtStart && i < 3) thickenAround(level, new BlockPos(px,py,pz));
                if (p1 >= 0) { py += sy; p1 -= 2*dx; }
                if (p2 >= 0) { pz += sz; p2 -= 2*dx; }
                p1 += 2*dy; p2 += 2*dz; px += sx;
            }
        } else if (dy >= dx && dy >= dz) {
            int p1 = 2*dx - dy, p2 = 2*dz - dy;
            for (int i = 0; i <= dy; i++) {
                placeLogStep(level, px, py, pz, x0, y0, z0);
                if (thickenAtStart && i < 3) thickenAround(level, new BlockPos(px,py,pz));
                if (p1 >= 0) { px += sx; p1 -= 2*dy; }
                if (p2 >= 0) { pz += sz; p2 -= 2*dy; }
                p1 += 2*dx; p2 += 2*dz; py += sy;
            }
        } else {
            int p1 = 2*dy - dz, p2 = 2*dx - dz;
            for (int i = 0; i <= dz; i++) {
                placeLogStep(level, px, py, pz, x0, y0, z0);
                if (thickenAtStart && i < 3) thickenAround(level, new BlockPos(px,py,pz));
                if (p1 >= 0) { py += sy; p1 -= 2*dz; }
                if (p2 >= 0) { px += sx; p2 -= 2*dz; }
                p1 += 2*dy; p2 += 2*dx; pz += sz;
            }
        }

        // small leaf accent at the end
        TreeStyles.placeLeavesIfReplaceable(level, DYNASTY_LEAVES(), b);
        if (rand.nextBoolean()) TreeStyles.placeLeavesIfReplaceable(level, DYNASTY_LEAVES(), b.above());
    }

    private void placeLogStep(WorldGenLevel level, int x, int y, int z, int xPrev, int yPrev, int zPrev) {
        Direction.Axis axis = dominantAxis(x, y, z, xPrev, yPrev, zPrev);
        TreeStyles.placeLogIfReplaceable(level, DYNASTY_LOG(), new BlockPos(x, y, z), axis);
    }

    private Direction.Axis dominantAxis(int x, int y, int z, int xPrev, int yPrev, int zPrev) {
        int ax = Math.abs(x - xPrev);
        int ay = Math.abs(y - yPrev);
        int az = Math.abs(z - zPrev);
        if (ay >= ax && ay >= az) return Direction.Axis.Y;
        return (ax >= az) ? Direction.Axis.X : Direction.Axis.Z;
    }

    private void thickenAround(WorldGenLevel level, BlockPos p) {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            TreeStyles.placeLogIfReplaceable(level, DYNASTY_LOG(), p.relative(d), d.getAxis());
        }
    }

    /* ---------- roots ---------- */

    private void makeRoots(WorldGenLevel level, BlockPos origin, RandomSource rand) {
        BlockPos[] bases = { origin, origin.offset(1,0,0), origin.offset(0,0,1), origin.offset(1,0,1) };
        for (BlockPos b : bases) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
            int len = 2 + rand.nextInt(3);
            BlockPos p = b;
            for (int i = 0; i < len; i++) {
                p = p.relative(dir);
                if (rand.nextFloat() < 0.45f) p = p.below();
                TreeStyles.placeLogIfReplaceable(level, DYNASTY_LOG(), p, (dir.getAxis()==Direction.Axis.X)?Direction.Axis.X:Direction.Axis.Z);
                if (rand.nextFloat() < 0.25f) TreeStyles.placeLeavesIfReplaceable(level, DYNASTY_LEAVES(), p.above());
            }
        }
    }
}
