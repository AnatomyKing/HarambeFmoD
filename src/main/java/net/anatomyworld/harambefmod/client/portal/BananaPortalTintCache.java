package net.anatomyworld.harambefmod.client.portal;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Client-side cache used to pre-tint Banana Portal panes as soon as the server
 * broadcasts SyncPortalTintPayload. This avoids the brief white pane at the
 * ignition spot before BEs sync.
 *
 * IMPORTANT: Keep usages on the physical client only (client packet handlers,
 * color handlers, screens, etc.).
 */
public final class BananaPortalTintCache {
    private static final Long2IntOpenHashMap CACHE = new Long2IntOpenHashMap();

    private BananaPortalTintCache() {}

    /** Returns cached tint for this pos or -1 if none. */
    public static int get(BlockPos pos) {
        return CACHE.getOrDefault(pos.asLong(), -1);
    }

    /** Clears all cache. */
    public static void clearAll() {
        CACHE.clear();
    }

    /** Fill a rect region with the given rgb and nudge the chunk renderer. */
    public static void fill(BlockPos anchor, Direction.Axis axis, int width, int height, int rgb) {
        Direction right = (axis == Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                BlockPos ip = anchor.relative(right, x).above(y);
                CACHE.put(ip.asLong(), rgb);
                invalidatePane(ip);
            }
        }
    }

    /** Remove a rect from the cache and nudge rendering. */
    public static void clearRect(BlockPos anchor, Direction.Axis axis, int width, int height) {
        Direction right = (axis == Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                BlockPos ip = anchor.relative(right, x).above(y);
                CACHE.remove(ip.asLong());
                invalidatePane(ip);
            }
        }
    }

    /** Tell the chunk renderer that the block changed so tint updates immediately. */
    private static void invalidatePane(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        LevelRenderer renderer = mc.levelRenderer;
        BlockState state = level.getBlockState(pos);

        // 1.21.x 5-arg variant: (BlockGetter, BlockPos, oldState, newState, flags)
        renderer.blockChanged(level, pos, state, state, 3);
    }
}
