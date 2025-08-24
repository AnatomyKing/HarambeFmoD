package net.anatomyworld.harambefmod.entity;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.entity.bananacow.BananaCow;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {

    // NeoForge 1.21.6+ convenience helper for entities
    public static final DeferredRegister.Entities ENTITIES =
            DeferredRegister.createEntities(HarambeCore.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<BananaCow>> BANANA_COW =
            ENTITIES.registerEntityType(
                    "banana_cow",
                    BananaCow::new,
                    MobCategory.CREATURE,
                    b -> b.sized(0.9F, 1.4F)
            );

    private static void onEntityAttributeEvent(EntityAttributeCreationEvent e) {
        e.put(BANANA_COW.get(), BananaCow.createAttributes().build());
    }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        bus.addListener(ModEntities::onEntityAttributeEvent);
    }
}
