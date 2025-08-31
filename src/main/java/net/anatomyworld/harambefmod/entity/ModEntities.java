package net.anatomyworld.harambefmod.entity;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.entity.boat.musavacca.MusavaccaBoat;
import net.anatomyworld.harambefmod.entity.mob.bananacow.BananaCow;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModEntities {

    // Entity-specific helper (adds nice sugar like registerEntityType)
    public static final DeferredRegister.Entities ENTITY_TYPES =
            DeferredRegister.createEntities(HarambeCore.MOD_ID);

    // Register the Banana Cow entity
    public static final Supplier<EntityType<BananaCow>> BANANA_COW =
            ENTITY_TYPES.registerEntityType(
                    "banana_cow",
                    BananaCow::new,
                    MobCategory.CREATURE,
                    b -> b.sized(0.9F, 1.4F)
            );

    public static final Supplier<EntityType<MusavaccaBoat>> MUSAVACCA_BOAT =
            ENTITY_TYPES.registerEntityType("musavacca_boat",
                    MusavaccaBoat::new, MobCategory.MISC,
                    b -> b.sized(1.875F, 0.875F).clientTrackingRange(10));


    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        modBus.addListener(ModEntities::onAttributes);
    }

    private static void onAttributes(final EntityAttributeCreationEvent e) {
        e.put(BANANA_COW.get(), BananaCow.createAttributes().build());
    }

    private ModEntities() {}
}
