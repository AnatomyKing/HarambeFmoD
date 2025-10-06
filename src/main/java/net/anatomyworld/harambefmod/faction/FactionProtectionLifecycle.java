package net.anatomyworld.harambefmod.faction;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Re-check protection immediately on lifecycle transitions:
 * - login / respawn / clone (death) / dimension change
 * - teleports (command, pearl, spreadplayers, chorus fruit via ItemConsumption)
 *
 * For teleports we schedule reevaluation on the server thread AFTER the move.
 */
public final class FactionProtectionLifecycle {
    private FactionProtectionLifecycle() {}

    /* ----------- Player lifecycle ----------- */

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            FactionProtectionGameplay.reevaluateNow(sp);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            FactionProtectionGameplay.reevaluateNow(sp);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            // brand new player entity after death: start clean (no residual effect/GM)
            FactionProtectionGameplay.resetProtectionState(sp);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            FactionProtectionGameplay.reevaluateNow(sp);
        }
    }

    /* ----------- Teleports ----------- */

    // /tp, /teleport, etc.
    @SubscribeEvent
    public static void onTeleportCommand(EntityTeleportEvent.TeleportCommand e) {
        if (e.getEntity() instanceof ServerPlayer sp && sp.getServer() != null) {
            sp.getServer().execute(() -> FactionProtectionGameplay.reevaluateNow(sp));
        }
    }

    // Ender pearl
    @SubscribeEvent
    public static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl e) {
        if (e.getEntity() instanceof ServerPlayer sp && sp.getServer() != null) {
            sp.getServer().execute(() -> FactionProtectionGameplay.reevaluateNow(sp));
        }
    }

    // /spreadplayers (and friends)
    @SubscribeEvent
    public static void onSpreadPlayersTeleport(EntityTeleportEvent.SpreadPlayersCommand e) {
        if (e.getEntity() instanceof ServerPlayer sp && sp.getServer() != null) {
            sp.getServer().execute(() -> FactionProtectionGameplay.reevaluateNow(sp));
        }
    }

    // Chorus fruit → fires as a teleport caused by an item being consumed.
    @SubscribeEvent
    public static void onItemConsumptionTeleport(EntityTeleportEvent.ItemConsumption e) {
        if (e.getEntity() instanceof ServerPlayer sp && sp.getServer() != null) {
            sp.getServer().execute(() -> FactionProtectionGameplay.reevaluateNow(sp));
        }
    }
}
