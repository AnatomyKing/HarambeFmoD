// src/main/java/net/anatomyworld/harambefmod/event/CrossDimPortalHandler.java
package net.anatomyworld.harambefmod.event;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.PortalForcer;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.anatomyworld.harambefmod.HarambeCore.LOGGER;

public final class CrossDimPortalHandler {
    private CrossDimPortalHandler() {}

    /** Small guard to avoid reprocessing the same entity multiple times in a couple ticks. */
    private static final Map<UUID, Integer> LAST_REROUTE_TICK = new ConcurrentHashMap<>();

    /** Call once during mod init. */
    public static void register() {
        NeoForge.EVENT_BUS.register(CrossDimPortalHandler.class);
        if (LOGGER.isDebugEnabled()) LOGGER.debug("[harambefmod] CrossDimPortalHandler registered");
    }

    /** Remember legit arrivals to overworld-like dims (covers commands/other mods). */
    @SubscribeEvent
    public static void onPlayerChangedDim(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        if (net.anatomyworld.harambefmod.Config.isOverworldLike(e.getTo())) {
            PortalMemory.rememberIfOverworldLike(sp, e.getTo(), true);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[harambefmod] rememberIfOverworldLike: {} -> {}",
                        sp.getGameProfile().getName(), e.getTo().location());
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[harambefmod] onPlayerChangedDim: {} arrived in non-overworld-like {}",
                    sp.getGameProfile().getName(), e.getTo().location());
        }
    }

    /**
     * All-entity logic:
     * - Into Nether FROM an overworld-like: remember source.
     * - From Nether TO Overworld: if we have a remembered overworld-like, CANCEL VANILLA FIRST and try reroute.
     *   If reroute fails, do nothing (entity stays in portal). No vanilla fallback to minecraft:overworld.
     */
    @SubscribeEvent
    public static void onTravel(EntityTravelToDimensionEvent e) {
        Entity entity = e.getEntity();
        if (!(entity.level() instanceof ServerLevel from)) return;

        var toKey = e.getDimension();
        MinecraftServer server = from.getServer();

        // Going into Nether from any overworld-like: remember source.
        if (toKey == Level.NETHER && net.anatomyworld.harambefmod.Config.isOverworldLike(from.dimension())) {
            PortalMemory.rememberIfOverworldLike(entity, from.dimension(), true);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[harambefmod] ENTER Nether: remembered source for {} = {}",
                        nameOf(entity), from.dimension().location());
            }
            return; // let vanilla proceed
        }

        // Leaving Nether -> Overworld: forcibly route back to the remembered overworld-like (if present).
        if (from.dimension() == Level.NETHER && toKey == Level.OVERWORLD) {
            int now = server.getTickCount();
            if (recentlyRerouted(entity.getUUID(), now)) {
                e.setCanceled(true);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[harambefmod] REROUTE GUARD: cancel again for {} (tick {})",
                            nameOf(entity), now);
                }
                return;
            }

            var rememberedOpt = PortalMemory.get(entity);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[harambefmod] EXIT Nether: remembered for {} = {}",
                        nameOf(entity), rememberedOpt.map(k -> k.location().toString()).orElse("<none>"));
            }

            var remembered = rememberedOpt
                    .filter(net.anatomyworld.harambefmod.Config::isOverworldLike)
                    .orElse(null);

            if (remembered == null) {
                // Per your requirement: do NOT fallback to Overworld — cancel and do nothing.
                e.setCanceled(true);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[harambefmod] EXIT Nether: no overworld-like remembered for {} → cancel vanilla, no move",
                            nameOf(entity));
                }
                return;
            }

            // Per requirement: cancel vanilla FIRST; then attempt reroute.
            e.setCanceled(true);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[harambefmod] EXIT Nether: attempting reroute for {} to {}",
                        nameOf(entity), remembered.location());
            }

            boolean ok = rerouteNetherReturn(entity, from, remembered);
            if (ok) {
                LAST_REROUTE_TICK.put(entity.getUUID(), now);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[harambefmod] REROUTE SUCCESS: {} -> {}", nameOf(entity), remembered.location());
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[harambefmod] REROUTE FAIL: {} stayed at Nether portal; no vanilla fallback",
                            nameOf(entity));
                }
            }
        }
    }

    private static boolean recentlyRerouted(UUID id, int currentTick) {
        Integer last = LAST_REROUTE_TICK.get(id);
        return last != null && currentTick - last <= 2;
    }

    private static boolean rerouteNetherReturn(Entity entity, ServerLevel from, net.minecraft.resources.ResourceKey<Level> targetKey) {
        MinecraftServer srv = from.getServer();
        ServerLevel dest = srv.getLevel(targetKey);
        if (dest == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[harambefmod] REROUTE ABORT: target dimension missing: {}", targetKey.location());
            }
            return false;
        }

        double scale = DimensionType.getTeleportationScale(from.dimensionType(), dest.dimensionType());
        double tx = entity.getX() * scale;
        double tz = entity.getZ() * scale;

        WorldBorder border = dest.getWorldBorder();
        tx = Mth.clamp(tx, border.getMinX() + 16.0, border.getMaxX() - 16.0);
        tz = Mth.clamp(tz, border.getMinZ() + 16.0, border.getMaxZ() - 16.0);
        int ty = Mth.floor(entity.getY());

        BlockPos search = BlockPos.containing(tx, ty, tz);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[harambefmod] REROUTE: scale={}, searchPos={} in {}",
                    String.format("%.4f", scale), fmt(search), dest.dimension().location());
        }

        PortalForcer forcer = dest.getPortalForcer();

        Optional<BlockPos> existing = PortalCompat.findClosestPortal(forcer, search, /*destIsNether*/ false, border);
        BlockPos portalPos;
        if (existing.isPresent()) {
            portalPos = existing.get();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[harambefmod] REROUTE: found existing portal at {}", fmt(portalPos));
            }
        } else {
            Optional<BlockUtil.FoundRectangle> created = PortalCompat.createPortal(forcer, search, Direction.Axis.X);
            if (created.isPresent()) {
                existing = PortalCompat.findClosestPortal(forcer, search, false, border);
                portalPos = existing.orElse(search);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[harambefmod] REROUTE: created portal; base at {}", fmt(portalPos));
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[harambefmod] REROUTE ABORT: could not create portal near {}", fmt(search));
                }
                return false;
            }
        }

        double px = portalPos.getX() + 0.5;
        double py = portalPos.getY() + 0.1;
        double pz = portalPos.getZ() + 0.5;

        boolean moved = PortalCompat.crossDimTeleport(entity, dest, px, py, pz, entity.getYRot(), entity.getXRot());
        if (!moved && LOGGER.isDebugEnabled()) {
            LOGGER.debug("[harambefmod] REROUTE ABORT: teleport failed for {} to ({}, {}, {}) in {}",
                    nameOf(entity),
                    String.format("%.2f", px), String.format("%.2f", py), String.format("%.2f", pz),
                    dest.dimension().location());
        }
        return moved;
    }

    // ---- tiny helpers for tidy logs ----

    private static String nameOf(Entity e) {
        return (e instanceof ServerPlayer sp) ? sp.getGameProfile().getName() : e.getStringUUID();
        // getName() can be expensive for non-players; UUID is fine for logs.
    }

    private static String fmt(BlockPos p) {
        return "(" + p.getX() + "," + p.getY() + "," + p.getZ() + ")";
    }
}
