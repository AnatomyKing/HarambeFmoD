// src/main/java/net/anatomyworld/harambefmod/event/SleepSkipCommandFallback.java
package net.anatomyworld.harambefmod.event;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Brutal fallback: when all sleepers trigger the sleep-finished event in one of our
 * "overworld-like" dimensions, execute a vanilla time command IN THAT DIMENSION.
 *
 * This guarantees the sky updates without relying on any vanilla cross-dim logic.
 */
public final class SleepSkipCommandFallback {
    private static final Logger LOG = LoggerFactory.getLogger("harambefmod");

    private SleepSkipCommandFallback() {}

    /** Call once during mod init. */
    public static void register() {
        NeoForge.EVENT_BUS.register(SleepSkipCommandFallback.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // Only in your curated list (Config.isOverworldLike)
        if (!net.anatomyworld.harambefmod.Config.isOverworldLike(level.dimension())) return;

        // Run the vanilla command IN THIS DIMENSION, next tick (safe & silent)
        level.getServer().execute(() -> {
            var server = level.getServer();
            var src = server.createCommandSourceStack()
                    .withLevel(level)            // <-- scope to THIS dimension
                    .withSuppressedOutput()      // no chat spam
                    .withPermission(4);          // op-level source

            // Early morning is 1000 ticks; feel free to swap to "time set day"
            server.getCommands().performPrefixedCommand(src, "time set 1000");

            // If you want the classic “clear after sleep”, uncomment:
            // server.getCommands().performPrefixedCommand(src, "weather clear");

            LOG.debug("[harambefmod] SleepSkipCommandFallback: ran 'time set 1000' in {}", level.dimension().location());
        });
    }
}
