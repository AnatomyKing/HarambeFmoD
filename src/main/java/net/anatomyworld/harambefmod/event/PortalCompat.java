package net.anatomyworld.harambefmod.event;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/** Compatibility helpers. Defaults to RAW (non-portal) teleports to avoid vanilla portal side effects. */
final class PortalCompat {
    private PortalCompat() {}

    /**
     * Cross-dimension teleport with explicit control over the "viaPortal" flag.
     * Use viaPortal=false for custom reroutes to avoid vanilla's portal state machine nudging placement.
     */
    static boolean crossDimTeleport(Entity e, ServerLevel dest, double x, double y, double z, float yaw, float pitch, boolean viaPortal) {
        // Preferred: typed 1.21.x API
        try {
            Set<Relative> flags = EnumSet.noneOf(Relative.class); // absolute pos/rot
            if (e instanceof net.minecraft.server.level.ServerPlayer sp) {
                sp.teleportTo(dest, x, y, z, flags, yaw, pitch, viaPortal);
                return true;
            } else {
                e.teleportTo(dest, x, y, z, flags, yaw, pitch, viaPortal);
                return true;
            }
        } catch (Throwable ignore) {
            // fall back to reflection for odd mapping environments
        }

        // Reflection fallback: portal-aware overload
        try {
            Class<?> rel = Class.forName("net.minecraft.world.entity.Relative");
            @SuppressWarnings({"rawtypes","unchecked"})
            Set flags = EnumSet.noneOf((Class) rel);

            Method m = e.getClass().getMethod(
                    "teleportTo",
                    ServerLevel.class, double.class, double.class, double.class, Set.class, float.class, float.class, boolean.class
            );
            Object ok = m.invoke(e, dest, x, y, z, flags, yaw, pitch, viaPortal);
            return !(ok instanceof Boolean) || (Boolean) ok;
        } catch (NoSuchMethodException ex) {
            // Older-style 7-arg fallback (no viaPortal param)
            try {
                Class<?> rel = Class.forName("net.minecraft.world.entity.RelativeMovement");
                @SuppressWarnings({"rawtypes","unchecked"})
                Set flags = EnumSet.noneOf((Class) rel);

                Method m = e.getClass().getMethod(
                        "teleportTo",
                        ServerLevel.class, double.class, double.class, double.class, Set.class, float.class, float.class
                );
                Object ok = m.invoke(e, dest, x, y, z, flags, yaw, pitch);
                return !(ok instanceof Boolean) || (Boolean) ok;
            } catch (Throwable ignore2) {
                return false;
            }
        } catch (Throwable t) {
            return false;
        }
    }

    /** Convenience: RAW by default (no vanilla portal behavior). */
    static boolean crossDimTeleportRaw(Entity e, ServerLevel dest, double x, double y, double z, float yaw, float pitch) {
        return crossDimTeleport(e, dest, x, y, z, yaw, pitch, /* viaPortal */ false);
    }

    /** Convenience: vanilla-portal semantics when explicitly wanted. */
    static boolean crossDimTeleportViaPortal(Entity e, ServerLevel dest, double x, double y, double z, float yaw, float pitch) {
        return crossDimTeleport(e, dest, x, y, z, yaw, pitch, /* viaPortal */ true);
    }

    @SuppressWarnings("unchecked")
    static Optional<BlockPos> findClosestPortal(PortalForcer forcer, BlockPos search, boolean destIsNether, WorldBorder border) {
        try {
            Method m = forcer.getClass().getMethod("findClosestPortalPosition", BlockPos.class, boolean.class, WorldBorder.class);
            return (Optional<BlockPos>) m.invoke(forcer, search, destIsNether, border);
        } catch (NoSuchMethodException ignored) {
            try {
                Method m = forcer.getClass().getMethod("getPortalPos", BlockPos.class, boolean.class, WorldBorder.class);
                return (Optional<BlockPos>) m.invoke(forcer, search, destIsNether, border);
            } catch (Exception ex) {
                return Optional.empty();
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    static Optional<BlockUtil.FoundRectangle> createPortal(PortalForcer forcer, BlockPos search, Direction.Axis axis) {
        try { return forcer.createPortal(search, axis); }
        catch (Throwable t) { return Optional.empty(); }
    }

    /** Intentionally NO vanilla portal cooldown; custom callers should guard re-entry themselves. */
    static void trySetPortalCooldown(Entity e) {
        // NO-OP on purpose to avoid cross-system side effects
    }
}
