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

    // Colors set to requested ARGB (0xFFRRGGBB) — matches overlay hues.
    public static final DeferredHolder<MobEffect, ProtectionMobEffect> BELMONT_PROTECTION =
            EFFECTS.register("belmont_protection",  () -> new ProtectionMobEffect(0xFF14B002)); // #14B002

    public static final DeferredHolder<MobEffect, ProtectionMobEffect> DYNASTY_PROTECTION =
            EFFECTS.register("dynasty_protection",  () -> new ProtectionMobEffect(0xFFA10E0F)); // #A10E0F

    public static final DeferredHolder<MobEffect, ProtectionMobEffect> IMPERIUM_PROTECTION =
            EFFECTS.register("imperium_protection", () -> new ProtectionMobEffect(0xFF2C55A7)); // #2C55A7

    public static final DeferredHolder<MobEffect, ProtectionMobEffect> MISCHIEF_PROTECTION =
            EFFECTS.register("mischief_protection", () -> new ProtectionMobEffect(0xFF7E2870)); // #7E2870

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
