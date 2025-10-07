package net.anatomyworld.harambefmod.mixin;

import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NeoForge 1.21.8 (Mojang mappings).
 * Completely disables the profiler pie chart by:
 *  - forcing showProfilerChart() to always return false
 *  - cancelling toggleProfilerChart() so the flag can never flip
 */
@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayNoPieMixin {

    @Inject(method = "showProfilerChart", at = @At("HEAD"), cancellable = true)
    private void harambe$neverShowProfiler(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "toggleProfilerChart", at = @At("HEAD"), cancellable = true)
    private void harambe$blockProfilerToggle(CallbackInfo ci) {
        ci.cancel();
    }
}
