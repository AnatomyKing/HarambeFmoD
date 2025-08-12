package net.anatomyworld.harambefmod.item;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HarambeCore.MOD_ID);

    // Optional: skip specific registry paths from showing up in the tab
    private static final Set<String> BLACKLIST = Set.of(
            // "pearl_fire",        // example if you ever give it a BlockItem but don't want it visible
            // "banana_cow_egg"     // (no default BlockItem anyway)
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HARAMBE_TAB =
            CREATIVE_TABS.register("harambe_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + HarambeCore.MOD_ID + ".harambe_tab"))
                    .icon(() -> new ItemStack(ModItems.BANANA_PEARL.get()))
                    .displayItems((params, output) -> {
                        List<Item> mine = new ArrayList<>();
                        for (Item item : BuiltInRegistries.ITEM) {
                            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                            if (id != null
                                    && HarambeCore.MOD_ID.equals(id.getNamespace())
                                    && !BLACKLIST.contains(id.getPath())) {
                                mine.add(item);
                            }
                        }
                        // nice stable order by registry path
                        mine.sort(Comparator.comparing(i -> BuiltInRegistries.ITEM.getKey(i).getPath()));
                        mine.forEach(output::accept);
                    })
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_TABS.register(bus);
    }

    private ModCreativeTabs() {}
}
