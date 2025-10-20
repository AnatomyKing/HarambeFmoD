package net.anatomyworld.harambefmod.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBoat.class)
public abstract class AbstractBoatLargeMountsMixin {

    @Unique
    private static boolean harambe$isAdultLargeMount(Entity e) {
        if (e instanceof AbstractHorse h) return !h.isBaby();
        if (e instanceof Camel c) return !c.isBaby();
        return false;
    }

    /**
     * Allow exactly one "large mount" (adult AbstractHorse or adult Camel) per boat.
     * Still keep the boat as a 2-seater so a player can join alongside.
     */
    @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
    private void harambe$boardingRules(Entity candidate, CallbackInfoReturnable<Boolean> cir) {
        AbstractBoat self = (AbstractBoat) (Object) this;

        if (harambe$isAdultLargeMount(candidate)) {
            boolean largeAlreadyAboard = self.getPassengers().stream()
                    .anyMatch(AbstractBoatLargeMountsMixin::harambe$isAdultLargeMount);
            if (largeAlreadyAboard) {
                // Block a second large mount.
                cir.setReturnValue(false);
                return;
            }
            // Allow the large mount if a seat is free (keep two seats total).
            cir.setReturnValue(self.getPassengers().size() < 2);
        }
        // Non-large mobs: let vanilla logic decide.
    }

    /**
     * Size gate: force-allow these large mounts so the boat doesn't reject them on dimensions.
     */
    @Inject(method = "hasEnoughSpaceFor", at = @At("HEAD"), cancellable = true)
    private void harambe$spaceWhitelist(Entity candidate, CallbackInfoReturnable<Boolean> cir) {
        if (harambe$isAdultLargeMount(candidate)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Seat position: center the large mount ONLY when it is the sole passenger.
     * If/when a second passenger joins, vanilla left/right offsets apply.
     */
    @Inject(method = "getPassengerAttachmentPoint", at = @At("HEAD"), cancellable = true)
    private void harambe$centerWhenSolo(Entity passenger, EntityDimensions dims, float partialTick,
                                        CallbackInfoReturnable<Vec3> cir) {
        AbstractBoat self = (AbstractBoat) (Object) this;
        if (harambe$isAdultLargeMount(passenger) && self.getPassengers().size() == 1) {
            // X/Z center; small Y raise so it sits visually on the hull. Adjust if needed.
            cir.setReturnValue(new Vec3(0.0D, 0.25D, 0.0D));
        }
    }
}
