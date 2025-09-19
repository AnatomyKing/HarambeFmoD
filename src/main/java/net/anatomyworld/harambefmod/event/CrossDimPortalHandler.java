// src/main/java/net/anatomy world/harambefmod/portal/CrossDimPortalHandler.java
package net.anatomyworld.harambefmod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.core.Direction;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.EnumSet;
import java.util.Optional;

public final class CrossDimPortalHandler {
    // call this from your mod init
    public static void register() {
        NeoForge.EVENT_BUS.register(CrossDimPortalHandler.class);
    }

    // Keep memory updated if player legitimately arrives in any overworld-like dim
    @SubscribeEvent
    public static void onPlayerChangedDim(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        ResourceKey<Level> to = e.getTo();
        if (net.anatomyworld.harambefmod.Config.isOverworldLike(to)) {
            PortalMemory.rememberIfOverworldLike(sp, to, true);
        }
    }

    @SubscribeEvent
    public static void onTravel(EntityTravelToDimensionEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;

        ServerLevel from = sp.serverLevel();
        ResourceKey<Level> toKey = e.getDimension();

        // If going INTO Nether from any overworld-like, remember the source
        if (toKey == Level.NETHER && net.anatomyworld.harambefmod.Config.isOverworldLike(from.dimension())) {
            PortalMemory.rememberIfOverworldLike(sp, from.dimension(), true);
            return; // let vanilla handle the actual trip to Nether
        }

        // If vanilla wants to send back to minecraft:overworld from the Nether, reroute
        if (from.dimension() == Level.NETHER && toKey == Level.OVERWORLD) {
            ResourceKey<Level> target = PortalMemory.get(sp)
                    .filter(net.anatomyworld.harambefmod.Config::isOverworldLike)
                    .orElse(Level.OVERWORLD);

            // If target is just vanilla overworld, let vanilla proceed
            if (target == Level.OVERWORLD) return;

            // Cancel vanilla and do our own portal-style teleport
            e.setCanceled(true);
            sendThroughNetherReturn(sp, from, target);
        }
    }

    private static void sendThroughNetherReturn(ServerPlayer sp, ServerLevel from, ResourceKey<Level> targetKey) {
        MinecraftServer srv = sp.getServer();
        ServerLevel dest = srv.getLevel(targetKey);
        if (dest == null) {
            // Fallback if dimension missing -> vanilla overworld
            dest = srv.getLevel(Level.OVERWORLD);
            if (dest == null) return;
        }

        // Vanilla scaling between Nether and dest (handles custom scales too)
        double scale = DimensionType.getTeleportationScale(from.dimensionType(), dest.dimensionType());
        double tx = sp.getX() * scale;
        double tz = sp.getZ() * scale;

        // Respect world border like vanilla does (keeps search inside safe area)
        var border = dest.getWorldBorder();
        tx = Mth.clamp(tx, border.getMinX() + 16.0, border.getMaxX() - 16.0);
        tz = Mth.clamp(tz, border.getMinZ() + 16.0, border.getMaxZ() - 16.0);
        int ty = Mth.floor(sp.getY()); // y doesn't scale in vanilla Nether logic

        BlockPos search = BlockPos.containing(tx, ty, tz);

        // Find or create a portal at/near the scaled coords, just like vanilla
        PortalForcer forcer = new PortalForcer(dest);
        Optional<BlockPos> portalBase = forcer.getPortalPos(search, false, border);
        if (portalBase.isEmpty()) {
            // Axis guess; vanilla derives this from the source portal, but X is fine
            portalBase = forcer.createPortal(search, Direction.Axis.X);
        }
        BlockPos portal = portalBase.orElse(search);

        // Center of the portal block; keep player rotation
        double px = portal.getX() + 0.5;
        double py = portal.getY() + 0.1;
        double pz = portal.getZ() + 0.5;

        // Absolute teleport (no relative flags)
        sp.teleportTo(dest, px, py, pz, EnumSet.noneOf(RelativeMovement.class), sp.getYRot(), sp.getXRot());
        // Optional (API varies): if available, prevent instant bounce
        // sp.setPortalCooldown();
    }
}
