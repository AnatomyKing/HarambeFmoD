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
import net.minecraft.world.level.block.Blocks;
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

/**
 * Vanilla-like Nether portal linking with Overworld pass-through:
 *
 * - Only runs if the entity is intersecting a NETHER_PORTAL block (so Banana Portals are never touched).
 * - Entering Nether from an overworld-like: remember that source dim.
 * - Leaving Nether -> Overworld:
 *      * If remembered == minecraft:overworld    -> let VANILLA handle it (no cancel).
 *      * If remembered == other overworld-like   -> CANCEL vanilla and route to that dimension.
 *      * If no memory                           -> let VANILLA handle it (no cancel).
 *
 * Uses vanilla PortalForcer search/creation and DimensionType teleportation scale.
 */
public final class CrossDimPortalHandler {
    private CrossDimPortalHandler() {}

    private static final Map<UUID, Integer> LAST_REROUTE_TICK = new ConcurrentHashMap<>();

    /** Call once during mod init. */
    public static void register() {
        NeoForge.EVENT_BUS.register(CrossDimPortalHandler.class);
        if (LOGGER.isDebugEnabled()) LOGGER.debug("[harambefmod] CrossDimPortalHandler registered");
    }

    /** When a player legitimately arrives in any overworld-like, remember it (helps command/other-mod moves too). */
    @SubscribeEvent
    public static void onPlayerChangedDim(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        if (net.anatomyworld.harambefmod.Config.isOverworldLike(e.getTo())) {
            PortalMemory.rememberIfOverworldLike(sp, e.getTo(), true);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[harambefmod] rememberIfOverworldLike: {} -> {}",
                        sp.getGameProfile().getName(), e.getTo().location());
            }
        }
    }

    @SubscribeEvent
    public static void onTravel(EntityTravelToDimensionEvent e) {
        Entity entity = e.getEntity();
        if (!(entity.level() instanceof ServerLevel from)) return;

        final var toKey = e.getDimension();
        final MinecraftServer server = from.getServer();

        // Only react to REAL Nether portal usage; Banana Portals never use this block.
        if (!isIntersectingNetherPortal((ServerLevel) entity.level(), entity)) return;

        // A) Overworld-like -> Nether: remember source, let vanilla proceed.
        if (toKey == Level.NETHER && net.anatomyworld.harambefmod.Config.isOverworldLike(from.dimension())) {
            PortalMemory.rememberIfOverworldLike(entity, from.dimension(), true);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[harambefmod] ENTER Nether: remembered source for {} = {}",
                        nameOf(entity), from.dimension().location());
            }
            return;
        }

        // B) Nether -> Overworld: Overworld pass-through unless memory is a different overworld-like.
        if (from.dimension() == Level.NETHER && toKey == Level.OVERWORLD) {
            int now = server.getTickCount();
            if (recentlyRerouted(entity.getUUID(), now)) {
                e.setCanceled(true);
                return;
            }

            var rememberedOpt = PortalMemory.get(entity)
                    .filter(net.anatomyworld.harambefmod.Config::isOverworldLike);

            // Let VANILLA handle if no memory OR memory is exactly minecraft:overworld
            if (rememberedOpt.isEmpty() || rememberedOpt.get() == Level.OVERWORLD) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[harambefmod] EXIT Nether: {} → vanilla Overworld handling (memory: {})",
                            nameOf(entity), rememberedOpt.map(k -> k.location().toString()).orElse("<none>"));
                }
                return; // no cancel
            }

            // Otherwise route to the remembered non-Overworld overworld-like.
            var remembered = rememberedOpt.get();
            e.setCanceled(true);
            boolean ok = rerouteNetherReturn(entity, from, remembered);
            if (ok) {
                LAST_REROUTE_TICK.put(entity.getUUID(), now);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[harambefmod] REROUTE SUCCESS: {} -> {}", nameOf(entity), remembered.location());
                }
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[harambefmod] REROUTE FAIL: {} stayed in the Nether portal; canceled vanilla",
                        nameOf(entity));
            }
        }
    }

    // --- helpers ---

    private static boolean recentlyRerouted(UUID id, int currentTick) {
        Integer last = LAST_REROUTE_TICK.get(id);
        return last != null && currentTick - last <= 2;
    }

    /** Tight AABB scan for Nether portal blocks around the entity. */
    private static boolean isIntersectingNetherPortal(ServerLevel level, Entity e) {
        var box = e.getBoundingBox().inflate(1.0E-4);
        int x0 = Mth.floor(box.minX), y0 = Mth.floor(box.minY), z0 = Mth.floor(box.minZ);
        int x1 = Mth.floor(box.maxX), y1 = Mth.floor(box.maxY), z1 = Mth.floor(box.maxZ);

        for (int x = x0; x <= x1; x++) for (int y = y0; y <= y1; y++) for (int z = z0; z <= z1; z++) {
            if (level.getBlockState(new BlockPos(x, y, z)).is(Blocks.NETHER_PORTAL)) return true;
        }
        return false;
    }

    private static boolean rerouteNetherReturn(Entity entity, ServerLevel from, net.minecraft.resources.ResourceKey<Level> targetKey) {
        MinecraftServer srv = from.getServer();
        ServerLevel dest = srv.getLevel(targetKey);
        if (dest == null) return false;

        // 8:1 scaling preserved via DimensionType API (vanilla behavior)
        double scale = net.minecraft.world.level.dimension.DimensionType.getTeleportationScale(from.dimensionType(), dest.dimensionType());
        double tx = entity.getX() * scale;
        double tz = entity.getZ() * scale;

        WorldBorder border = dest.getWorldBorder();
        tx = net.minecraft.util.Mth.clamp(tx, border.getMinX() + 16.0, border.getMaxX() - 16.0);
        tz = net.minecraft.util.Mth.clamp(tz, border.getMinZ() + 16.0, border.getMaxZ() - 16.0);
        int ty = net.minecraft.util.Mth.floor(entity.getY());

        BlockPos search = BlockPos.containing(tx, ty, tz);
        PortalForcer forcer = dest.getPortalForcer();

        Optional<BlockPos> existing = PortalCompat.findClosestPortal(forcer, search, /*destIsNether*/ false, border);
        BlockPos portalPos;
        if (existing.isPresent()) {
            portalPos = existing.get();
        } else {
            Direction.Axis yawAxis = Direction.fromYRot(entity.getYRot()).getAxis();
            Optional<BlockUtil.FoundRectangle> created = PortalCompat.createPortal(forcer, search, yawAxis);
            if (created.isPresent()) {
                existing = PortalCompat.findClosestPortal(forcer, search, false, border);
                portalPos = existing.orElse(search);
            } else {
                return false;
            }
        }

        double px = portalPos.getX() + 0.5;
        double py = portalPos.getY() + 0.1;
        double pz = portalPos.getZ() + 0.5;

        // ⬇️ KEY CHANGE: do a RAW teleport (viaPortal=false) to avoid vanilla portal nudging.
        boolean moved = PortalCompat.crossDimTeleportRaw(entity, dest, px, py, pz, entity.getYRot(), entity.getXRot());
        if (!moved) return false;

        // ⬇️ NO vanilla setPortalCooldown — your Banana portal already gates re-entry; our tick guard
        // in this handler prevents immediate re-processing on Nether exit.
        // PortalCompat.trySetPortalCooldown(entity);  // removed on purpose

        return true;
    }

    private static String nameOf(Entity e) {
        return (e instanceof ServerPlayer sp) ? sp.getGameProfile().getName() : e.getStringUUID();
    }
}
