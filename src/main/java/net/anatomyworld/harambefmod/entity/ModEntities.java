package net.anatomyworld.harambefmod.entity;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.entity.bananacow.BananaCow;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HarambeCore.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<BananaCow>> BANANA_COW =
            ENTITIES.register("banana_cow",
                    () -> EntityType.Builder.<BananaCow>of(BananaCow::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.4F)
                            .build(HarambeCore.MOD_ID + ":banana_cow"));

    private static void onEntityAttributeEvent(EntityAttributeCreationEvent e) {
        e.put(BANANA_COW.get(), BananaCow.createAttributes().build());

    }



    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        bus.addListener(ModEntities::onEntityAttributeEvent);
    }
}
