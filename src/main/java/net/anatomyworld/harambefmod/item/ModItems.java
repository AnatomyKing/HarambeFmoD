package net.anatomyworld.harambefmod.item;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.entity.ModEntities;
import net.anatomyworld.harambefmod.item.custom.FlintAndPearlItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(HarambeCore.MOD_ID);

    public static final DeferredItem<Item> BANANA_PEARL =
            ITEMS.register("banana_pearl",
                    () -> new Item(new Item.Properties()
                            .food(Foods.GOLDEN_CARROT)
                    ));

    public static final DeferredItem<Item> BANANA =
            ITEMS.register("banana",
                    () -> new Item(new Item.Properties()
                            .food(Foods.APPLE)
                    ));

    public static final DeferredItem<Item> ANYTOMITHIUM_INGOT =
            ITEMS.register("anytomithium_ingot",
                    () -> new Item(new Item.Properties()
                            .rarity(Rarity.RARE)
                    ));

    public static final DeferredItem<Item> RAW_ANYTOMITHIUM =
            ITEMS.register("raw_anytomithium",
                    () -> new Item(new Item.Properties()
                            .rarity(Rarity.RARE)
                    ));

    public static final DeferredItem<Item> ANYPHONE =
            ITEMS.register("anyphone",
                    () -> new Item(new Item.Properties()
                            .rarity(Rarity.EPIC)
                    ));

    public static final DeferredItem<Item> FLINT_AND_PEARL =
            ITEMS.register("flint_and_pearl",
                    () -> new FlintAndPearlItem(
                            new Item.Properties()
                                    .durability(128)
                                    .rarity(Rarity.COMMON)));

    public static final DeferredItem<DeferredSpawnEggItem> BANANA_COW_SPAWN_EGG =
            ITEMS.register("banana_cow_spawn_egg",
                    () -> new DeferredSpawnEggItem(
                            ModEntities.BANANA_COW, // accepts DeferredHolder directly
                            0xFFE97B,               // egg base colour (banana yellow)
                            0x6F4E1F,               // spots colour   (stem brown)
                            new Item.Properties()));



    public static final DeferredItem<BlockItem> BANANA_COW_EGG_BLOCK_ITEM =
            ITEMS.register("banana_cow_egg",
                    () -> new BlockItem(ModBlocks.BANANA_COW_EGG_BLOCK.get(), new Item.Properties()));


    public static final DeferredItem<BlockItem> BANANA_PEARL_BLOCK_ITEM =
            ITEMS.register("banana_pearl_block",
                    () -> new BlockItem(ModBlocks.BANANA_PEARL_BLOCK.get(),
                            new Item.Properties()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
