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
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HarambeCore.MOD_ID);

    public static final DeferredItem<Item> BANANA_PEARL =
            ITEMS.registerItem("banana_pearl", props -> new Item(props.food(Foods.GOLDEN_CARROT)));

    public static final DeferredItem<Item> BANANA =
            ITEMS.registerItem("banana", props -> new Item(props.food(Foods.APPLE)));

    public static final DeferredItem<Item> ANYTOMITHIUM_INGOT =
            ITEMS.registerItem("anytomithium_ingot", props -> new Item(props.rarity(Rarity.RARE)));

    public static final DeferredItem<Item> RAW_ANYTOMITHIUM =
            ITEMS.registerItem("raw_anytomithium", props -> new Item(props.rarity(Rarity.RARE)));

    public static final DeferredItem<Item> ANYPHONE =
            ITEMS.registerItem("anyphone", props -> new Item(props.rarity(Rarity.EPIC)));

    public static final DeferredItem<Item> FLINT_AND_PEARL =
            ITEMS.registerItem("flint_and_pearl",
                    props -> new FlintAndPearlItem(props.durability(128).rarity(Rarity.COMMON)));

    // 1.21.8: use SpawnEggItem directly + registerItem so Item.Properties has id
    public static final DeferredItem<SpawnEggItem> BANANA_COW_SPAWN_EGG =
            ITEMS.registerItem("banana_cow_spawn_egg",
                    props -> new SpawnEggItem(ModEntities.BANANA_COW.get(), props));

    // Stage-specific egg items
    public static final DeferredItem<Item> BANANA_COW_EGG_UNRIPE =
            ITEMS.registerItem("banana_cow_egg_unripe",
                    props -> new BananaCowEggStageItem(ModBlocks.BANANA_COW_EGG.get(), props, 0));

    public static final DeferredItem<Item> BANANA_COW_EGG_RIPENING =
            ITEMS.registerItem("banana_cow_egg_ripening",
                    props -> new BananaCowEggStageItem(ModBlocks.BANANA_COW_EGG.get(), props, 1));

    public static final DeferredItem<Item> BANANA_COW_EGG_RIPE =
            ITEMS.registerItem("banana_cow_egg_ripe",
                    props -> new BananaCowEggStageItem(ModBlocks.BANANA_COW_EGG.get(), props, 2));

    // Replaces ItemNameBlockItem: helper creates a BlockItem with proper id set
    public static final DeferredItem<BlockItem> MUSAVACCA_SPROUT =
            ITEMS.registerSimpleBlockItem("musavacca_sprout", ModBlocks.MUSAVACCA_PLANT, new Item.Properties());

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
