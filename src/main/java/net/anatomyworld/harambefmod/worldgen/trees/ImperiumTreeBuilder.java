package net.anatomyworld.harambefmod.worldgen.trees;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.worldgen.TreeStyles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Imperium — Redwood/Pine silhouette (no lateral branches)
 * - Tall, straight trunk (2×2 lower, 1×1 spire)
 * - High, tapered conical crown (unified leaf mass)
 * - Minimal trunk-hugging leaf spurs for columnar density
 * - Subtle buttress pads at base (clean)
 *
 * Design notes:
 *  • Coast redwood: very straight trunk, canopy often begins far up the tree.
 *  • Conifers: stable pyramidal/conical crown.
 */
public final class ImperiumTreeBuilder implements TreeStyles.PatchTreeBuilder {

    /* ===== Tunables ===== */
    // Trunk + spire
    private static final int TRUNK_MIN_H = 20, TRUNK_MAX_H = 28;
    private static final int BASE_2X2_MIN = 6,  BASE_2X2_MAX = 9;   // lower 2×2 section height

    // Crown (high, tapered cone)
    private static final float CROWN_PORTION = 0.65f;               // upper ~65% holds the crown (redwood-like)
    private static final int   CROWN_BASE_R_MIN = 3, CROWN_BASE_R_MAX = 5;
    private static final double CROWN_TAPER_POWER = 1.15;           // >1 gives steeper taper
    private static final boolean ADD_SKIRT = true;                   // add a slightly wider ring under crown start

    // Columnar density: trunk leaf spurs (very light)
    private static final float TRUNK_SPUR_CHANCE = 0.22f;

    // Base pads
    private static final boolean BUTTRESS = true;

    private static BlockState LOG()    { return ModBlocks.IMPERIUM_LOG.get().defaultBlockState(); }
    private static BlockState LEAVES() { return ModBlocks.IMPERIUM_LEAVES.get().defaultBlockState(); }

    @Override
    public boolean place(WorldGenLevel level, BlockPos origin, RandomSource rand) {
        // 1) Straight trunk: 2×2 base, 1×1 spire
        int trunkH  = Mth.nextInt(rand, TRUNK_MIN_H, TRUNK_MAX_H);
        int base2x2 = Mth.nextInt(rand, BASE_2X2_MIN, BASE_2X2_MAX);
        buildTrunk(level, origin, trunkH, base2x2);

        if (BUTTRESS) buttressPads(level, origin);

        // 2) High conical crown
        int crownStartY = origin.getY() + Mth.floor(trunkH * (1.0f - CROWN_PORTION));
        int crownTopY   = origin.getY() + trunkH + 2; // small cap above trunk tip
        int baseR       = Mth.nextInt(rand, CROWN_BASE_R_MIN, CROWN_BASE_R_MAX);

        // optional slight "skirt" just below the crown start (pines/redwood lower crown impression)
        if (ADD_SKIRT) {
            leafDisk(level, new BlockPos(origin.getX(), crownStartY - 1, origin.getZ()), Math.max(1, baseR - 1), rand, true);
        }

        buildConicalCrown(level, new BlockPos(origin.getX(), crownStartY, origin.getZ()), crownTopY, baseR, rand);

        // 3) Sparse trunk spurs to read as dense column
        addTrunkSpurs(level, origin, trunkH, rand);

        return true;
    }

    @Override public String name() { return "Imperium"; }

    /* ===================== Trunk ===================== */

    private void buildTrunk(WorldGenLevel level, BlockPos origin, int trunkH, int base2x2) {
        // 2×2 lower
        for (int y = 0; y < Math.min(base2x2, trunkH); y++) {
            BlockPos base = origin.above(y);
            core2x2(level, base);
        }
        // 1×1 spire above
        for (int y = base2x2; y < trunkH; y++) {
            TreeStyles.placeLogIfReplaceable(level, LOG(), origin.above(y), Direction.Axis.Y);
        }
        // spear tip / leader
        TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), origin.above(trunkH));
        TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), origin.above(trunkH + 1));
    }

    private void core2x2(WorldGenLevel level, BlockPos base) {
        TreeStyles.placeLogIfReplaceable(level, LOG(), base, Direction.Axis.Y);
        TreeStyles.placeLogIfReplaceable(level, LOG(), base.east(), Direction.Axis.Y);
        TreeStyles.placeLogIfReplaceable(level, LOG(), base.south(), Direction.Axis.Y);
        TreeStyles.placeLogIfReplaceable(level, LOG(), base.east().south(), Direction.Axis.Y);
    }

    private void buttressPads(WorldGenLevel level, BlockPos origin) {
        // small pads in the four cardinal directions (one block out, 2 tall)
        Direction[] dirs = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (Direction d : dirs) {
            BlockPos f = origin.relative(d);
            TreeStyles.placeLogIfReplaceable(level, LOG(), f, Direction.Axis.Y);
            TreeStyles.placeLogIfReplaceable(level, LOG(), f.above(), Direction.Axis.Y);
        }
    }

    /* ===================== Crown (tapered cone) ===================== */

    private void buildConicalCrown(WorldGenLevel level, BlockPos crownBase, int topY, int baseR, RandomSource rand) {
        int baseY  = crownBase.getY();
        int height = Math.max(5, topY - baseY);

        for (int dy = 0; dy <= height; dy++) {
            int y = baseY + dy;
            double t = 1.0 - (dy / (double) height);                 // 1 → 0
            // non-linear taper for a sharper cone toward the top
            int r = Math.max(1, (int)Math.floor(baseR * Math.pow(t, CROWN_TAPER_POWER)));
            leafDisk(level, new BlockPos(crownBase.getX(), y, crownBase.getZ()), r, rand, false);

            // tiny vertical “needle” along the cone surface for texture (very sparse)
            if (r >= 3 && rand.nextFloat() < 0.08f) {
                BlockPos edge = new BlockPos(crownBase.getX() + (rand.nextBoolean() ? r : -r), y, crownBase.getZ());
                TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), edge.below());
            }
        }
        // small cap
        leafDisk(level, new BlockPos(crownBase.getX(), topY, crownBase.getZ()), 1, rand, false);
    }

    /**
     * Leaf disk with optional underside fill for a heavier “ring” (used by skirt).
     */
    private void leafDisk(WorldGenLevel level, BlockPos center, int r, RandomSource rand, boolean heavier) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx*dx + dz*dz <= r*r) {
                    BlockPos p = center.offset(dx, 0, dz);
                    TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), p);
                    if (heavier && rand.nextFloat() < 0.25f) {
                        TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), p.below());
                    } else if (!heavier && r >= 3 && rand.nextFloat() < 0.10f) {
                        TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), p.below()); // slight underside fullness
                    }
                }
            }
        }
    }

    private void addTrunkSpurs(WorldGenLevel level, BlockPos origin, int trunkH, RandomSource rand) {
        int startY = Mth.floor(trunkH * 0.45f);
        for (int y = startY; y < trunkH; y++) {
            if (rand.nextFloat() < TRUNK_SPUR_CHANCE) {
                Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
                BlockPos p = origin.above(y).relative(d);
                TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), p);
                if (rand.nextBoolean()) TreeStyles.placeLeavesIfReplaceable(level, LEAVES(), p.above());
            }
        }
    }
}
