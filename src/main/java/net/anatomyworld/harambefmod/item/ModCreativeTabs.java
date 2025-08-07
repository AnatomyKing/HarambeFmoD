package net.anatomyworld.harambefmod.item;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HarambeCore.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HARAMBE_TAB =
            CREATIVE_TABS.register("harambe_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + HarambeCore.MOD_ID + ".harambe_tab"))
                    .icon(() -> new ItemStack(ModItems.BANANA_PEARL.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BANANA_PEARL.get());
                        output.accept(ModItems.BANANA.get());
                        output.accept(ModItems.RAW_ANYTOMITHIUM.get());
                        output.accept(ModItems.ANYTOMITHIUM_INGOT.get());
                        output.accept(ModItems.ANYPHONE.get());
                        output.accept(ModItems.BANANA_COW_SPAWN_EGG.get());
                        output.accept(ModItems.GOLIATH_BEETLE_SPAWN_EGG.get());
                        output.accept(ModItems.BANANA_PEARL_BLOCK_ITEM.get());
                        output.accept(ModItems.BANANA_COW_EGG_BLOCK_ITEM.get());
                        output.accept(ModItems.FLINT_AND_PEARL.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_TABS.register(bus);
    }
}
