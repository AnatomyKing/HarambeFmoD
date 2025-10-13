package net.anatomyworld.harambefmod.faction;

import net.anatomyworld.harambefmod.effect.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class FactionAuraEffects {
    private FactionAuraEffects() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer sp) || sp.level().isClientSide) return;

        var dim = sp.level().dimension();
        BlockPos p = sp.blockPosition();
        var entry = CatalystRegistry.activeEntryAt(dim, p);
        if (entry == null) {
            removeAll(sp);
            return;
        }

        long now = System.currentTimeMillis();
        long remainingMs = Math.max(0L, entry.expiresAtMs() - now);
        if (remainingMs <= 0L) {
            removeAll(sp);
            return;
        }

        int ticks = (int) Math.min(Integer.MAX_VALUE, remainingMs / 50L);

        switch (entry.faction()) {
            case BELMONT  -> ensure(sp, ModMobEffects.BELMONT_PROTECTION, ticks);
            case DYNASTY  -> ensure(sp, ModMobEffects.DYNASTY_PROTECTION, ticks);
            case IMPERIUM -> ensure(sp, ModMobEffects.IMPERIUM_PROTECTION, ticks);
            case MISCHIEF -> ensure(sp, ModMobEffects.MISCHIEF_PROTECTION, ticks);
        }
    }

    private static void ensure(ServerPlayer sp, Holder<MobEffect> effect, int ticks) {
        MobEffectInstance cur = sp.getEffect(effect);
        // Hide particles, keep icon:
        // ambient=false, visible=false (no particles), showIcon=true
        if (cur == null || cur.getDuration() != ticks || cur.isVisible() || !cur.showIcon()) {
            sp.addEffect(new MobEffectInstance(effect, ticks, 0, false, false, true));
        }
        if (!effect.equals(ModMobEffects.BELMONT_PROTECTION))  sp.removeEffect(ModMobEffects.BELMONT_PROTECTION);
        if (!effect.equals(ModMobEffects.DYNASTY_PROTECTION))  sp.removeEffect(ModMobEffects.DYNASTY_PROTECTION);
        if (!effect.equals(ModMobEffects.IMPERIUM_PROTECTION)) sp.removeEffect(ModMobEffects.IMPERIUM_PROTECTION);
        if (!effect.equals(ModMobEffects.MISCHIEF_PROTECTION)) sp.removeEffect(ModMobEffects.MISCHIEF_PROTECTION);
    }

    private static void removeAll(ServerPlayer sp) {
        sp.removeEffect(ModMobEffects.BELMONT_PROTECTION);
        sp.removeEffect(ModMobEffects.DYNASTY_PROTECTION);
        sp.removeEffect(ModMobEffects.IMPERIUM_PROTECTION);
        sp.removeEffect(ModMobEffects.MISCHIEF_PROTECTION);
    }
}
