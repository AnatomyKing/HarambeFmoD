package net.anatomyworld.harambefmod.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class ProtectionMobEffect extends MobEffect {
    public ProtectionMobEffect(int argbColor) {
        super(MobEffectCategory.BENEFICIAL, argbColor);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return false; }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) { return false; }
}
