package net.anatomyworld.harambefmod.client.portal;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Client-side cache used to pre-tint Banana Portal panes as soon as the server
 * broadcasts SyncPortalTintPayload. This avoids the brief white pane at the
 * ignition spot before BEs sync.
 *
 * Keys are pos.asLong() -> rgb. Block color handler reads from this cache first.
 */
@OnlyIn(Dist.CLIENT)
public final class BananaPortalTintCache {
    private static final Long2IntOpenHashMap CACHE = new Long2IntOpenHashMap();

    private BananaPortalTintCache() {}

    /** Returns cached tint for this pos or -1 if none. */
    public static int get(BlockPos pos) {
        return CACHE.getOrDefault(pos.asLong(), -1);
    }

    /** Clears all cache (optional helper if you ever need it). */
    public static void clearAll() {
        CACHE.clear();
    }

    /**
     * Fill the whole interior with the given rgb tint and invalidate the renderer
     * so the color takes effect immediately on the client.
     */
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

    /** Remove a rect from the cache (e.g., on portal clear) and invalidate. */
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

    /**
     * IMPORTANT FIX: use the 5-arg LevelRenderer#blockChanged(...) signature
     * (BlockGetter, BlockPos, oldState, newState, flags).
     *
     * Passing the current state for both old/new is fine; we just want to nudge
     * the chunk renderer. A common flags value is 3.
     */
    private static void invalidatePane(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        LevelRenderer renderer = mc.levelRenderer;
        BlockState state = level.getBlockState(pos);

        // 5-arg method call (this is what fixes your compile error):
        renderer.blockChanged(level, pos, state, state, 3);
    }
}
