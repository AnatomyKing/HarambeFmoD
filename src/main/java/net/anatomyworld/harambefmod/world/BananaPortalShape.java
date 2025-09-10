package net.anatomyworld.harambefmod.world;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.custom.BananaPortalBlock;
import net.anatomyworld.harambefmod.block.entity.BananaPortalBlockEntity;
import net.anatomyworld.harambefmod.data.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/** Vanilla-like detector (width 2..21, height 3..21). Corners optional. */
public final class BananaPortalShape {

    public enum FrameMode { INTRA, INTER }

    public record Frame(Direction.Axis axis, BlockPos interiorBottomLeft, int width, int height, FrameMode mode) {
        public BlockPos anchor() { return interiorBottomLeft; }
    }

    private static boolean isInterior(LevelAccessor level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        return s.isAir() || s.is(ModBlocks.PEARL_FIRE.get()) || s.is(ModBlocks.BANANA_PORTAL.get());
    }

    private static boolean isFrameInMode(LevelAccessor level, BlockPos pos, FrameMode mode) {
        BlockState s = level.getBlockState(pos);
        return mode == FrameMode.INTER
                ? s.is(ModTags.Blocks.BANANA_PORTAL_FRAME_INTER)
                : s.is(ModTags.Blocks.BANANA_PORTAL_FRAME);
    }

    private static Optional<FrameMode> frameModeOf(LevelAccessor level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        boolean intra = s.is(ModTags.Blocks.BANANA_PORTAL_FRAME);
        boolean inter = s.is(ModTags.Blocks.BANANA_PORTAL_FRAME_INTER);
        if (intra == inter) return Optional.empty(); // either none or both -> invalid/ambiguous
        return Optional.of(inter ? FrameMode.INTER : FrameMode.INTRA);
    }

    public static Optional<Frame> find(ServerLevel level, BlockPos origin) {
        var fx = findAxis(level, origin, Direction.Axis.X);
        return fx.isPresent() ? fx : findAxis(level, origin, Direction.Axis.Z);
    }

    private static Optional<Frame> findAxis(ServerLevel level, BlockPos start, Direction.Axis axis) {
        Direction right = (axis == Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;
        Direction left  = right.getOpposite();

        BlockPos p = start.immutable();
        int fall = 0;
        while (fall < 21 && isInterior(level, p) && frameModeOf(level, p.below()).isEmpty()) { p = p.below(); fall++; }
        var belowMode = frameModeOf(level, p.below());
        if (!isInterior(level, p) || belowMode.isEmpty()) return Optional.empty();
        FrameMode mode = belowMode.get();

        int slide = 0;
        while (slide < 21 && isInterior(level, p) && !isFrameInMode(level, p.relative(left), mode)) { p = p.relative(left); slide++; }
        if (!isInterior(level, p) || !isFrameInMode(level, p.relative(left), mode)) return Optional.empty();

        int width = 1;
        BlockPos cursor = p;
        while (width <= 21) {
            if (isFrameInMode(level, cursor.relative(right), mode)) break;
            cursor = cursor.relative(right);
            if (!isInterior(level, cursor)) return Optional.empty();
            width++;
        }
        if (width < 2 || width > 21 || !isFrameInMode(level, cursor.relative(right), mode)) return Optional.empty();

        int height = 1;
        BlockPos rowBase = p;
        while (height <= 21) {
            if (!isFrameInMode(level, rowBase.relative(left), mode) || !isFrameInMode(level, rowBase.relative(right, width), mode)) return Optional.empty();
            for (int dx = 0; dx < width; dx++) if (!isInterior(level, rowBase.relative(right, dx))) return Optional.empty();

            BlockPos next = rowBase.above();
            boolean topSpan = true;
            for (int dx = 0; dx < width; dx++) if (!isFrameInMode(level, next.relative(right, dx), mode)) { topSpan = false; break; }
            if (topSpan) break;

            rowBase = next;
            height++;
        }
        if (height < 3 || height > 21) return Optional.empty();

        BlockPos bottom = p.below();
        for (int dx = 0; dx < width; dx++) if (!isFrameInMode(level, bottom.relative(right, dx), mode)) return Optional.empty();

        return Optional.of(new Frame(axis, p.immutable(), width, height, mode));
    }

    /** Fill interior: set AXIS, color, anchor on each BE. */
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
                }
            }
        }
    }

    /** Validate frame (sides + spans) still intact around any interior block. */
    public static boolean isInteriorStillFramed(ServerLevel level, BlockPos anyInterior) {
        BlockState s = level.getBlockState(anyInterior);
        if (!s.is(ModBlocks.BANANA_PORTAL.get())) return true;

        BlockPos p = anyInterior;
        while (level.getBlockState(p.below()).is(ModBlocks.BANANA_PORTAL.get())) p = p.below();

        boolean hasX = level.getBlockState(p.west()).is(ModBlocks.BANANA_PORTAL.get())
                || level.getBlockState(p.east()).is(ModBlocks.BANANA_PORTAL.get());
        Direction.Axis axis = hasX ? Direction.Axis.X : Direction.Axis.Z;
        Direction right = (axis == Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;
        Direction left  = right.getOpposite();

        while (level.getBlockState(p.relative(left)).is(ModBlocks.BANANA_PORTAL.get())) p = p.relative(left);

        int width = 0;
        while (width < 64 && level.getBlockState(p.relative(right, width)).is(ModBlocks.BANANA_PORTAL.get())) width++;
        int height = 0;
        while (height < 64 && level.getBlockState(p.above(height)).is(ModBlocks.BANANA_PORTAL.get())) height++;

        for (int y = 0; y < height; y++) {
            BlockPos row = p.above(y);
            if (!isFrameBlock(level, row.relative(left))) return false;
            if (!isFrameBlock(level, row.relative(right, width))) return false;
        }
        for (int dx = 0; dx < width; dx++) {
            if (!isFrameBlock(level, p.above(height).relative(right, dx))) return false;
            if (!isFrameBlock(level, p.below().relative(right, dx))) return false;
        }
        return true;
    }

    private static boolean isFrameBlock(LevelAccessor level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        return s.is(ModTags.Blocks.BANANA_PORTAL_FRAME) || s.is(ModTags.Blocks.BANANA_PORTAL_FRAME_INTER);
    }

    private BananaPortalShape() {}
}
