package net.anatomyworld.harambefmod.world;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/** Programmatic placement for the Musavacca tree (no templates). */
public final class MusavaccaTreePlacer {

    private MusavaccaTreePlacer() {}

    /** Build the tree with trunk base at {@code origin}. */
    public static boolean place(ServerLevel level, BlockPos origin) {
        // 1) Remove the sapling without drops, then set rooted dirt beneath the trunk
        clearSaplingNoDrops(level, origin);
        setRootedDirtBelow(level, origin);

        // 2) Place the hardcoded layout
        boolean ok = placeFromSpec(level, origin);

        // 3) Immediately fix leaf distances in the 5×7×5 footprint so nothing decays
        if (ok) {
            recomputeLeafDistancesInBox(level, origin, 5, 7, 5);
        }
        return ok;
    }

    private static void clearSaplingNoDrops(ServerLevel level, BlockPos origin) {
        BlockState cur = level.getBlockState(origin);
        if (cur.is(BlockTags.SAPLINGS)) { // vanilla saplings tag
            level.setBlock(origin, Blocks.AIR.defaultBlockState(),
                    Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS); // no loot from the sapling
        }
    }

    private static void setRootedDirtBelow(ServerLevel level, BlockPos origin) {
        BlockPos below = origin.below();
        level.setBlock(below, Blocks.ROOTED_DIRT.defaultBlockState(),
                Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
    }

    /* ---------- programmatic placement (matches your NBT spec, plus N/S leaves at y=4) ---------- */

    private static boolean isSoftForPlacement(BlockState s) {
        return s.isAir()
                || s.canBeReplaced()
                || s.is(BlockTags.LEAVES)
                || s.is(BlockTags.SAPLINGS)
                || s.is(Blocks.SNOW) || s.is(Blocks.VINE)
                || s.is(Blocks.FERN) || s.is(Blocks.LARGE_FERN) || s.is(Blocks.TALL_GRASS);
    }

    private static void setIfSoft(ServerLevel level, BlockPos pos, BlockState state) {
        if (isSoftForPlacement(level.getBlockState(pos))) {
            level.setBlock(pos, state, Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
        }
    }

    /** origin aligns with structure (2,0,2). */
    private static boolean placeFromSpec(ServerLevel level, BlockPos origin) {
        BlockState stemY = ModBlocks.MUSAVACCA_STEM.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState stemX = ModBlocks.MUSAVACCA_STEM.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        BlockState stemZ = ModBlocks.MUSAVACCA_STEM.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);

        // leaves: let vanilla manage DISTANCE; we’ll recompute right after placement
        BlockState leaves = ModBlocks.MUSAVACCA_LEAVES.get().defaultBlockState();
        BlockState crown  = ModBlocks.MUSAVACCA_LEAVES_CROWN.get().defaultBlockState();
        BlockState flower = ModBlocks.MUSAVACCA_FLOWER.get().defaultBlockState();

        var place    = (java.util.function.BiConsumer<BlockPos, BlockState>) (pos, state) -> setIfSoft(level, pos, state);
        var placeRel = (java.util.function.BiConsumer<int[], BlockState>) (xyz, state) ->
                place.accept(origin.offset(xyz[0] - 2, xyz[1], xyz[2] - 2), state);

        // y=0..5 vertical trunk at (2,*,2)
        for (int y = 0; y <= 5; y++) placeRel.accept(new int[]{2, y, 2}, stemY);

        // y=2 flowers N/E/S/W
        placeRel.accept(new int[]{1, 2, 2}, flower);
        placeRel.accept(new int[]{2, 2, 1}, flower);
        placeRel.accept(new int[]{2,  2, 3}, flower);
        placeRel.accept(new int[]{3,  2, 2}, flower);

        // y=3 arms + crown corners
        placeRel.accept(new int[]{1, 3, 2}, stemX);
        placeRel.accept(new int[]{3, 3, 2}, stemX);
        placeRel.accept(new int[]{2, 3, 1}, stemZ);
        placeRel.accept(new int[]{2, 3, 3}, stemZ);
        placeRel.accept(new int[]{1, 3, 1}, crown);
        placeRel.accept(new int[]{1, 3, 3}, crown);
        placeRel.accept(new int[]{3, 3, 1}, crown);
        placeRel.accept(new int[]{3, 3, 3}, crown);

        // y=4 ring of leaves + arms + trunk (adds N/S extremes)
        placeRel.accept(new int[]{0, 4, 2}, leaves); // W extreme
        placeRel.accept(new int[]{4, 4, 2}, leaves); // E extreme
        placeRel.accept(new int[]{2, 4, 0}, leaves); // N extreme (added)
        placeRel.accept(new int[]{2, 4, 4}, leaves); // S extreme (added)

        placeRel.accept(new int[]{1, 4, 1}, leaves);
        placeRel.accept(new int[]{1, 4, 3}, leaves);
        placeRel.accept(new int[]{3, 4, 1}, leaves);
        placeRel.accept(new int[]{3, 4, 3}, leaves);

        placeRel.accept(new int[]{1, 4, 2}, stemX);
        placeRel.accept(new int[]{3, 4, 2}, stemX);
        placeRel.accept(new int[]{2, 4, 1}, stemZ);
        placeRel.accept(new int[]{2, 4, 3}, stemZ);
        placeRel.accept(new int[]{2, 4, 2}, stemY);

        // y=5 leaf cross + trunk
        placeRel.accept(new int[]{1, 5, 2}, leaves);
        placeRel.accept(new int[]{2, 5, 1}, leaves);
        placeRel.accept(new int[]{2, 5, 2}, stemY);
        placeRel.accept(new int[]{2, 5, 3}, leaves);
        placeRel.accept(new int[]{3, 5, 2}, leaves);

        // y=6 crown top
        placeRel.accept(new int[]{2, 6, 2}, crown);

        return true;
    }

    /* ---------- Leaf distance recompute (public-property approach) ---------- */

    /**
     * Recomputes {@link LeavesBlock#DISTANCE} for all leaves inside a box of size (sx×sy×sz).
     * Structure origin is (origin-2, origin.y, origin-2).
     * Algorithm: multi-source BFS from all logs across leaf nodes (6-way), clamped to 1..7.
     */
    private static void recomputeLeafDistancesInBox(ServerLevel level, BlockPos origin, int sx, int sy, int sz) {
        BlockPos min = origin.offset(-2, 0, -2);

        // Collect leaves & logs in the footprint
        Set<BlockPos> leafSet = new HashSet<>();
        Deque<BlockPos> logQueue = new ArrayDeque<>();

        for (int y = 0; y < sy; y++) {
            for (int x = 0; x < sx; x++) {
                for (int z = 0; z < sz; z++) {
                    BlockPos p = min.offset(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (s.is(BlockTags.LOGS)) {
                        logQueue.add(p.immutable());
                    } else if (s.getBlock() instanceof LeavesBlock) {
                        leafSet.add(p.immutable());
                    }
                }
            }
        }

        // BFS distances from logs to leaves (through leaves only)
        Map<BlockPos, Integer> dist = new HashMap<>();
        Deque<BlockPos> q = new ArrayDeque<>(logQueue); // logs are distance 0 (implicit)

        while (!q.isEmpty()) {
            BlockPos cur = q.poll();
            int base = dist.getOrDefault(cur, 0);
            if (base > 6) continue; // Leaves distance ranges 1..7; 6 here means next leaves become 7

            for (Direction d : Direction.values()) {
                BlockPos n = cur.relative(d);
                if (!leafSet.contains(n)) continue;  // traverse inside our placed leaves
                int nd = base + 1;
                Integer prev = dist.get(n);
                if (prev == null || nd < prev) {
                    dist.put(n, nd);
                    if (nd <= 6) q.add(n);
                }
            }
        }

        // Write distances (unreached => 7)
        for (BlockPos p : leafSet) {
            BlockState s = level.getBlockState(p);
            if (!(s.getBlock() instanceof LeavesBlock)) continue;
            int d = dist.getOrDefault(p, 7);
            if (d < 1) d = 1;
            if (d > 7) d = 7;

            BlockState updated = s.setValue(LeavesBlock.DISTANCE, d);
            if (updated != s) {
                level.setBlock(p, updated, Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
        }
    }
}
