package net.anatomyworld.harambefmod.entity.bananacow;

import net.anatomyworld.harambefmod.entity.ModEntities;
import net.anatomyworld.harambefmod.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BananaCow extends Cow {

    public BananaCow(EntityType<? extends BananaCow> type, Level level) {
        super(type, level);
    }

    /* ------------------------------------------------------------------ */
    /*  Attributes                                                         */
    /* ------------------------------------------------------------------ */
    public static @NotNull AttributeSupplier.Builder createAttributes() {
        return Cow.createLivingAttributes()
                .add(Attributes.MAX_HEALTH,    10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE,  16.0D);
    }

    /* ------------------------------------------------------------------ */
    /*  Breeding – always produce a baby Banana Cow                        */
    /* ------------------------------------------------------------------ */
    @Override
    public @NotNull BananaCow getBreedOffspring(@NotNull ServerLevel level,
                                                @NotNull AgeableMob partner) {
        return Objects.requireNonNull(
                ModEntities.BANANA_COW.get().create(level));
    }

    /* ------------------------------------------------------------------ */
    /*  Custom adult-only 50 % banana drop                                 */
    /*  (new 1.21.x signature → ServerLevel comes first, there is *no*     */
    /*   looting-level parameter any more)                                 */
    /* ------------------------------------------------------------------ */
    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level,
                                       @NotNull DamageSource source,
                                       boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);

        if (!this.isBaby() && this.getRandom().nextFloat() < 0.25F) {
            this.spawnAtLocation(ModItems.BANANA.get());
        }
    }
}
