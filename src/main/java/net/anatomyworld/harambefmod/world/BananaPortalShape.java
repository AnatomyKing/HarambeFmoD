package net.anatomyworld.harambefmod.world;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.custom.BananaPortalBlock;
import net.anatomyworld.harambefmod.block.entity.BananaPortalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Vanilla-like frame detector (interior width 2..21, height 3..21).
 * Sides + top + bottom must be Banana Pearl. Corners are optional.
 * Interior may be AIR / PEARL_FIRE / BANANA_PORTAL.
 */
public final class BananaPortalShape {

    public record Frame(Direction.Axis axis, BlockPos interiorBottomLeft, int width, int height) {
        public BlockPos anchor() { return interiorBottomLeft; }
    }

    private static boolean isFrame(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.BANANA_PEARL_BLOCK.get());
    }

    private static boolean isInterior(LevelAccessor level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        return s.isAir() || s.is(ModBlocks.PEARL_FIRE.get()) || s.is(ModBlocks.BANANA_PORTAL.get());
    }

    /** Try both axes at this position. */
    public static Optional<Frame> find(ServerLevel level, BlockPos origin) {
        var fx = findAxis(level, origin, Direction.Axis.X);
        return fx.isPresent() ? fx : findAxis(level, origin, Direction.Axis.Z);
    }

    private static Optional<Frame> findAxis(ServerLevel level, BlockPos start, Direction.Axis axis) {
        Direction right = (axis == Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;
        Direction left  = right.getOpposite();

        // Step down in interior until the block below is frame
        BlockPos p = start.immutable();
        int fall = 0;
        while (fall < 21 && isInterior(level, p) && !isFrame(level, p.below())) { p = p.below(); fall++; }
        if (!isInterior(level, p) || !isFrame(level, p.below())) return Optional.empty();

        // Slide left until left neighbor is frame (bottom-left interior)
        int slide = 0;
        while (slide < 21 && isInterior(level, p) && !isFrame(level, p.relative(left))) { p = p.relative(left); slide++; }
        if (!isInterior(level, p) || !isFrame(level, p.relative(left))) return Optional.empty();

        // Measure interior width
        int width = 1;
        BlockPos cursor = p;
        while (width <= 21) {
            if (isFrame(level, cursor.relative(right))) break;
            cursor = cursor.relative(right);
            if (!isInterior(level, cursor)) return Optional.empty();
            width++;
        }
        if (width < 2 || width > 21 || !isFrame(level, cursor.relative(right))) return Optional.empty();

        // Measure interior height while validating side columns
        int height = 1;
        BlockPos rowBase = p;
        while (height <= 21) {
            if (!isFrame(level, rowBase.relative(left)) || !isFrame(level, rowBase.relative(right, width))) return Optional.empty();

            // Row interior must be interior
            for (int dx = 0; dx < width; dx++) {
                if (!isInterior(level, rowBase.relative(right, dx))) return Optional.empty();
            }

            // Is next row the top span?
            BlockPos next = rowBase.above();
            boolean topSpan = true;
            for (int dx = 0; dx < width; dx++) {
                if (!isFrame(level, next.relative(right, dx))) { topSpan = false; break; }
            }
            if (topSpan) break;
            rowBase = next;
            height++;
        }
        if (height < 3 || height > 21) return Optional.empty();

        // Bottom span must be frame too
        BlockPos bottom = p.below();
        for (int dx = 0; dx < width; dx++) {
            if (!isFrame(level, bottom.relative(right, dx))) return Optional.empty();
        }

        return Optional.of(new Frame(axis, p.immutable(), width, height));
    }

    /** Fill the interior with portal blocks; set AXIS and color, plus anchor & (per-BE) front is set by caller. */
    public static void fill(ServerLevel level, Frame f, int rgb) {
        Direction right = (f.axis == Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;
        for (int y = 0; y < f.height; y++) {
            for (int x = 0; x < f.width; x++) {
                BlockPos ip = f.interiorBottomLeft.relative(right, x).above(y);
                level.setBlock(ip, ModBlocks.BANANA_PORTAL.get()
                        .defaultBlockState()
                        .setValue(BananaPortalBlock.AXIS, f.axis), Block.UPDATE_ALL);
                var be = level.getBlockEntity(ip);
                if (be instanceof BananaPortalBlockEntity pbe) {
                    pbe.setColor(rgb);
                    pbe.setAnchor(f.anchor());
                    // front direction is set in the flint item after we decide it (we may call setFront again there)
                }
            }
        }
    }

    /** Validate frame around any interior block. */
    public static boolean isInteriorStillFramed(ServerLevel level, BlockPos anyInterior) {
        BlockState s = level.getBlockState(anyInterior);
        if (!s.is(ModBlocks.BANANA_PORTAL.get())) return true;

        // Slide down to the bottom interior
        BlockPos p = anyInterior;
        while (level.getBlockState(p.below()).is(ModBlocks.BANANA_PORTAL.get())) p = p.below();

        boolean hasX = level.getBlockState(p.west()).is(ModBlocks.BANANA_PORTAL.get())
                || level.getBlockState(p.east()).is(ModBlocks.BANANA_PORTAL.get());
        Direction.Axis axis = hasX ? Direction.Axis.X : Direction.Axis.Z;
        Direction right = (axis == Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;
        Direction left  = right.getOpposite();

        // Slide to interior bottom-left
        while (level.getBlockState(p.relative(left)).is(ModBlocks.BANANA_PORTAL.get())) p = p.relative(left);

        // Measure interior size by contiguous portal blocks
        int width = 0;
        while (width < 64 && level.getBlockState(p.relative(right, width)).is(ModBlocks.BANANA_PORTAL.get())) width++;
        int height = 0;
        while (height < 64 && level.getBlockState(p.above(height)).is(ModBlocks.BANANA_PORTAL.get())) height++;

        // Side columns must be Banana Pearl
        for (int y = 0; y < height; y++) {
            BlockPos row = p.above(y);
            if (!level.getBlockState(row.relative(left)).is(ModBlocks.BANANA_PEARL_BLOCK.get())) return false;
            if (!level.getBlockState(row.relative(right, width)).is(ModBlocks.BANANA_PEARL_BLOCK.get())) return false;
        }
        // Top & bottom spans across interior (corners optional)
        for (int dx = 0; dx < width; dx++) {
            if (!level.getBlockState(p.above(height).relative(right, dx)).is(ModBlocks.BANANA_PEARL_BLOCK.get())) return false;
            if (!level.getBlockState(p.below().relative(right, dx)).is(ModBlocks.BANANA_PEARL_BLOCK.get())) return false;
        }
        return true;
    }

    private BananaPortalShape() {}
}
