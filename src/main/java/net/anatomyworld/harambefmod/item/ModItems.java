package net.anatomyworld.harambefmod.item;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.entity.ModEntities;
import net.anatomyworld.harambefmod.item.custom.BananaCowEggStageItem;
import net.anatomyworld.harambefmod.item.custom.FlintAndPearlItem;

import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem; // ← use this in 1.21.8

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(HarambeCore.MOD_ID);

    public static final DeferredItem<Item> BANANA_PEARL =
            ITEMS.register("banana_pearl",
                    () -> new Item(new Item.Properties().food(Foods.GOLDEN_CARROT)));

    public static final DeferredItem<Item> BANANA =
            ITEMS.register("banana",
                    () -> new Item(new Item.Properties().food(Foods.APPLE)));

    public static final DeferredItem<Item> ANYTOMITHIUM_INGOT =
            ITEMS.register("anytomithium_ingot",
                    () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredItem<Item> RAW_ANYTOMITHIUM =
            ITEMS.register("raw_anytomithium",
                    () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredItem<Item> ANYPHONE =
            ITEMS.register("anyphone",
                    () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

    public static final DeferredItem<Item> FLINT_AND_PEARL =
            ITEMS.register("flint_and_pearl",
                    () -> new FlintAndPearlItem(
                            new Item.Properties()
                                    .durability(128)
                                    .rarity(Rarity.COMMON)));

    // Use SpawnEggItem directly in 1.21.8
    public static final DeferredItem<SpawnEggItem> BANANA_COW_SPAWN_EGG =
            ITEMS.registerItem("banana_cow_spawn_egg",
                    props -> new SpawnEggItem(ModEntities.BANANA_COW.get(), props));

    // Stage-specific egg items (still fine)
    public static final DeferredItem<Item> BANANA_COW_EGG_UNRIPE =
            ITEMS.register("banana_cow_egg_unripe",
                    () -> new BananaCowEggStageItem(ModBlocks.BANANA_COW_EGG.get(),
                            new Item.Properties(), 0));

    public static final DeferredItem<Item> BANANA_COW_EGG_RIPENING =
            ITEMS.register("banana_cow_egg_ripening",
                    () -> new BananaCowEggStageItem(ModBlocks.BANANA_COW_EGG.get(),
                            new Item.Properties(), 1));

    public static final DeferredItem<Item> BANANA_COW_EGG_RIPE =
            ITEMS.register("banana_cow_egg_ripe",
                    () -> new BananaCowEggStageItem(ModBlocks.BANANA_COW_EGG.get(),
                            new Item.Properties(), 2));

    // Replace ItemNameBlockItem: use the helper so the item uses the BLOCK description key
    public static final DeferredItem<BlockItem> MUSAVACCA_SPROUT =
            ITEMS.registerSimpleBlockItem(
                    "musavacca_sprout",
                    ModBlocks.MUSAVACCA_PLANT,           // pass the Holder/DeferredBlock, NOT .get()
                    new Item.Properties()
            );

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
