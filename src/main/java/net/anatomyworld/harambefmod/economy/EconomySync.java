package net.anatomyworld.harambefmod.economy;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.network.BalanceSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Seed the client with their balance on login. */
@EventBusSubscriber(modid = HarambeCore.MOD_ID)
public final class EconomySync {
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        long bal = Economy.get(sp.getServer(), sp.getGameProfile());
        PacketDistributor.sendToPlayer(sp, new BalanceSyncPayload(bal));
    }
    private EconomySync() {}
}
