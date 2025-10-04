package net.anatomyworld.harambefmod.menu;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
// ↓ lets MenuType be created from an (id, inv, buf) factory
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public final class ModMenus {
    private ModMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, HarambeCore.MOD_ID);

    // Use IMenuTypeExtension.create so the client constructor receives FriendlyByteBuf
    public static final DeferredHolder<MenuType<?>, MenuType<SimpleChestMenu>> SIMPLE_CHEST =
            MENUS.register("simple_chest",
                    () -> IMenuTypeExtension.create(SimpleChestMenu::new));
}
