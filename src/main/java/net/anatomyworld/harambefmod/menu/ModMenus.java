// ModMenus.java
package net.anatomyworld.harambefmod.menu;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private ModMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, HarambeCore.MOD_ID);

    // Existing simple chest menu
    public static final DeferredHolder<MenuType<?>, MenuType<SimpleChestMenu>> SIMPLE_CHEST =
            MENUS.register("simple_chest",
                    () -> IMenuTypeExtension.create(SimpleChestMenu::new));

    // NEW: AnyChest menu (3x9 chest with custom texture)
    public static final DeferredHolder<MenuType<?>, MenuType<AnyChestMenu>> ANY_CHEST =
            MENUS.register("anytomithium_chest",
                    () -> IMenuTypeExtension.create(AnyChestMenu::new));
}
