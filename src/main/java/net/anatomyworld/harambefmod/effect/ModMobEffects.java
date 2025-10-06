package net.anatomyworld.harambefmod.effect;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMobEffects {
    private ModMobEffects() {}

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, HarambeCore.MOD_ID);

    public static final DeferredHolder<MobEffect, ProtectionMobEffect> MISCHIEF_PROTECTION =
            EFFECTS.register("mischief_protection", () -> new ProtectionMobEffect(0xFF8B5CF6));

    public static final DeferredHolder<MobEffect, ProtectionMobEffect> IMPERIUM_PROTECTION =
            EFFECTS.register("imperium_protection", () -> new ProtectionMobEffect(0xFFEAB308));

    public static final DeferredHolder<MobEffect, ProtectionMobEffect> DYNASTY_PROTECTION =
            EFFECTS.register("dynasty_protection", () -> new ProtectionMobEffect(0xFF22C55E));

    public static final DeferredHolder<MobEffect, ProtectionMobEffect> BELMONT_PROTECTION =
            EFFECTS.register("belmont_protection", () -> new ProtectionMobEffect(0xFFEF4444));

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
