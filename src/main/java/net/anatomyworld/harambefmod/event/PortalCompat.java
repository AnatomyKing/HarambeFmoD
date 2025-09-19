// src/main/java/net/anatomyworld/harambefmod/event/PortalCompat.java
package net.anatomyworld.harambefmod.event;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

final class PortalCompat {
    private PortalCompat() {}

    /**
     * Cross-dimension teleport using the best-available signature.
     * Prefers the portal-aware overload (… yaw, pitch, boolean viaPortal) on 1.21.x,
     * and falls back to the older 7-arg signature when needed.
     */
    static boolean crossDimTeleport(Entity e, ServerLevel dest, double x, double y, double z, float yaw, float pitch) {
        // Mojmap 1.21+: Relative enum; some environments still expose RelativeMovement
        Class<?> relativeEnum = tryClass("net.minecraft.world.entity.Relative");
        if (relativeEnum == null) relativeEnum = tryClass("net.minecraft.world.entity.RelativeMovement");
        if (relativeEnum == null) return false;

        @SuppressWarnings({"rawtypes","unchecked"})
        Set flags = EnumSet.noneOf((Class) relativeEnum);

        // 1) Try portal-aware overload:
        //    teleportTo(ServerLevel, double, double, double, Set<Relative>, float, float, boolean viaPortal)
        try {
            Method m = e.getClass().getMethod(
                    "teleportTo",
                    ServerLevel.class, double.class, double.class, double.class, Set.class, float.class, float.class, boolean.class
            );
            Object ok = m.invoke(e, dest, x, y, z, flags, yaw, pitch, /*viaPortal*/ true);
            return !(ok instanceof Boolean) || (Boolean) ok;
        } catch (NoSuchMethodException ignore) {
            // continue to 7-arg fallback
        } catch (Throwable t) {
            return false;
        }

        // 2) Fallback: 7-arg signature (no viaPortal flag)
        try {
            Method m = e.getClass().getMethod(
                    "teleportTo",
                    ServerLevel.class, double.class, double.class, double.class, Set.class, float.class, float.class
            );
            Object ok = m.invoke(e, dest, x, y, z, flags, yaw, pitch);
            return !(ok instanceof Boolean) || (Boolean) ok;
        } catch (Throwable t) {
            // 3) Last chance: some environments require resolving against Entity.class
            try {
                Class<?> entityCls = Class.forName("net.minecraft.world.entity.Entity");
                Method m = entityCls.getMethod(
                        "teleportTo",
                        ServerLevel.class, double.class, double.class, double.class, Set.class, float.class, float.class
                );
                Object ok = m.invoke(e, dest, x, y, z, flags, yaw, pitch);
                return !(ok instanceof Boolean) || (Boolean) ok;
            } catch (Throwable ignore) {
                return false;
            }
        }
    }

    /** findClosestPortalPosition / getPortalPos across mapping variants. */
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

    /** Create a portal rectangle (vanilla-style). */
    static Optional<BlockUtil.FoundRectangle> createPortal(PortalForcer forcer, BlockPos search, Direction.Axis axis) {
        try {
            return forcer.createPortal(search, axis);
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    /** Optional: try to apply a portal cooldown after teleport. */
    static void trySetPortalCooldown(Entity e) {
        try {
            Method m = e.getClass().getMethod("setPortalCooldown");
            m.invoke(e);
        } catch (Throwable ignored) {}
    }

    private static Class<?> tryClass(String fqcn) {
        try { return Class.forName(fqcn); } catch (Throwable t) { return null; }
    }
}
