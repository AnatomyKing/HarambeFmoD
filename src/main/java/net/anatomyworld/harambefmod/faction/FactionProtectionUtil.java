package net.anatomyworld.harambefmod.faction;

import net.anatomyworld.harambefmod.effect.ModMobEffects;
import net.minecraft.server.level.ServerPlayer;

public final class FactionProtectionUtil {
    private FactionProtectionUtil() {}

    public static void removeAllProtectionEffects(ServerPlayer sp) {
        sp.removeEffect(ModMobEffects.BELMONT_PROTECTION);
        sp.removeEffect(ModMobEffects.DYNASTY_PROTECTION);
        sp.removeEffect(ModMobEffects.IMPERIUM_PROTECTION);
        sp.removeEffect(ModMobEffects.MISCHIEF_PROTECTION);
    }
}
