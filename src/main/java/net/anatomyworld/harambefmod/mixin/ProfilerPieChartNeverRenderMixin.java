package net.anatomyworld.harambefmod.mixin;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Belts & suspenders: even if something toggles the chart on,
 * cancel the actual draw call.
 *
 * We target by string FQCN to avoid compile-time imports.
 */
@Mixin(targets = "net.minecraft.client.gui.components.debugchart.ProfilerPieChart")
public abstract class ProfilerPieChartNeverRenderMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void harambe$neverRenderPie(GuiGraphics g, CallbackInfo ci) {
        ci.cancel();
    }
}
