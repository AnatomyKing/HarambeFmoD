package net.anatomyworld.harambefmod.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBoat.class)
public abstract class BoatHappyGhastCargoMixin implements Leashable {

    // --- shadows we need from AbstractBoat ---
    @Shadow public abstract void setPaddleState(boolean left, boolean right);

    // --- helper: are we currently leashed to a HappyGhast? ---
    @Unique
    private boolean ghastTow$isHeldByHappyGhast() {
        Entity holder = ((Leashable)(Object)this).getLeashHolder();
        return holder instanceof HappyGhast;
    }

    // 1) Pretend there is NO controller while a HappyGhast is towing us.
    //    This makes the boat act like cargo (no player control branches).
    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
    private void ghastTow$nullControllerWhenGhastLeashed(CallbackInfoReturnable<LivingEntity> cir) {
        if (ghastTow$isHeldByHappyGhast()) {
            cir.setReturnValue(null);
        }
    }

    // 2) Block client-side rowing completely while towed by HappyGhast.
    //    Prevents client drift and paddle animation, and keeps state synced.
    @Inject(method = "controlBoat", at = @At("HEAD"), cancellable = true)
    private void ghastTow$blockClientControl(CallbackInfo ci) {
        if (ghastTow$isHeldByHappyGhast()) {
            this.setPaddleState(false, false);
            ci.cancel();
        }
    }

}
