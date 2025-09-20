package net.anatomyworld.harambefmod.item;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.entity.ModEntities;
import net.anatomyworld.harambefmod.item.boat.MusavaccaBoatItem;
import net.anatomyworld.harambefmod.item.custom.AnyPhoneItem;
import net.anatomyworld.harambefmod.item.custom.BananaCowEggStageItem;
import net.anatomyworld.harambefmod.item.custom.FlintAndPearlItem;
import net.anatomyworld.harambefmod.item.custom.ModArmorMaterials;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HarambeCore.MOD_ID);

    public static final DeferredItem<Item> BANANA_PEARL =
            ITEMS.registerItem("banana_pearl", props -> new Item(props.food(Foods.GOLDEN_CARROT)));

    public static final DeferredItem<Item> BANANA =
            ITEMS.registerItem("banana", props -> new Item(props.food(Foods.APPLE)));

    public static final DeferredItem<Item> PURPISH_ANYTOMITHIUM_INGOT =
            ITEMS.registerItem("purpish_anytomithium_ingot", props -> new Item(props.rarity(Rarity.RARE)));

    public static final DeferredItem<Item> TEALISH_ANYTOMITHIUM_INGOT =
            ITEMS.registerItem("tealish_anytomithium_ingot", props -> new Item(props.rarity(Rarity.RARE)));

    public static final DeferredItem<Item> RAW_ANYTOMITHIUM =
            ITEMS.registerItem("raw_anytomithium", props -> new Item(props.rarity(Rarity.RARE)));

    public static final DeferredItem<Item> CRYSTALLIZED_HONEY =
            ITEMS.registerItem("crystallized_honey", props -> new Item(props.rarity(Rarity.RARE)));

    public static final DeferredItem<Item> HONEY_CRYSTALLINE =
            ITEMS.registerItem("honey_crystalline", props -> new Item(props.rarity(Rarity.RARE)));

    public static final DeferredItem<Item> HONEY_CLUSTER =
            ITEMS.registerItem("honey_cluster", props -> new Item(props.rarity(Rarity.RARE)));

    public static final DeferredItem<Item> HONEY_CORE =
            ITEMS.registerItem("honey_core", props -> new Item(props.rarity(Rarity.EPIC)));

    public static final DeferredItem<Item> BELMONT_BANNER_PATTERN =
            ITEMS.registerItem("belmont_banner_pattern", props -> new Item(props.rarity(Rarity.COMMON)));
    public static final DeferredItem<Item> DYNASTY_BANNER_PATTERN =
            ITEMS.registerItem("dynasty_banner_pattern", props -> new Item(props.rarity(Rarity.COMMON)));
    public static final DeferredItem<Item> IMPERIUM_BANNER_PATTERN =
            ITEMS.registerItem("imperium_banner_pattern", props -> new Item(props.rarity(Rarity.COMMON)));
    public static final DeferredItem<Item> MISCHIEF_BANNER_PATTERN =
            ITEMS.registerItem("mischief_banner_pattern", props -> new Item(props.rarity(Rarity.COMMON)));

    public static final DeferredItem<Item> ANYPHONE =
            ITEMS.registerItem("anyphone", p -> new AnyPhoneItem(
                    p.rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredItem<Item> FLINT_AND_PEARL =
            ITEMS.registerItem("flint_and_pearl",
                    props -> new FlintAndPearlItem(props.durability(128).rarity(Rarity.COMMON)));

    public static final DeferredItem<Item> MUSAVACCA_BOAT_ITEM =
            ITEMS.registerItem("musavacca_boat",
                    props -> new MusavaccaBoatItem(props.stacksTo(1)));

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


    // -------------------- BELMONT --------------------
    public static final DeferredItem<Item> BELMONT_HELMET =
            ITEMS.registerItem("belmont_helmet", props -> {
                // Start with humanoid armor (sets durability/attributes/enchantability/repair)
                Item.Properties p = props.humanoidArmor(ModArmorMaterials.BELMONT, ArmorType.HELMET);
                // Override Equippable WITHOUT an asset so the item model renders on the head (pumpkin behavior)
                Equippable headEquip = Equippable.builder(EquipmentSlot.HEAD)
                        .setEquipSound(SoundEvents.ARMOR_EQUIP_CHAIN)
                        .setDamageOnHurt(true) // wear down like armor
                        .build();
                return new Item(p.component(DataComponents.EQUIPPABLE, headEquip));
            });

    public static final DeferredItem<Item> BELMONT_CHESTPLATE =
            ITEMS.registerItem("belmont_chestplate", p -> new Item(p.humanoidArmor(ModArmorMaterials.BELMONT, ArmorType.CHESTPLATE)));
    public static final DeferredItem<Item> BELMONT_LEGGINGS =
            ITEMS.registerItem("belmont_leggings", p -> new Item(p.humanoidArmor(ModArmorMaterials.BELMONT, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> BELMONT_BOOTS =
            ITEMS.registerItem("belmont_boots", p -> new Item(p.humanoidArmor(ModArmorMaterials.BELMONT, ArmorType.BOOTS)));

    // ================= ARMOR: DYNASTY =================
    public static final DeferredItem<Item> DYNASTY_HELMET =
            ITEMS.registerItem("dynasty_helmet", props -> {
                Item.Properties p = props.humanoidArmor(ModArmorMaterials.DYNASTY, ArmorType.HELMET);
                Equippable headEquip = Equippable.builder(EquipmentSlot.HEAD)
                        .setEquipSound(SoundEvents.ARMOR_EQUIP_CHAIN)
                        .setDamageOnHurt(true)
                        .build();
                return new Item(p.component(DataComponents.EQUIPPABLE, headEquip));
            });
    public static final DeferredItem<Item> DYNASTY_CHESTPLATE =
            ITEMS.registerItem("dynasty_chestplate", p -> new Item(p.humanoidArmor(ModArmorMaterials.DYNASTY, ArmorType.CHESTPLATE)));
    public static final DeferredItem<Item> DYNASTY_LEGGINGS =
            ITEMS.registerItem("dynasty_leggings", p -> new Item(p.humanoidArmor(ModArmorMaterials.DYNASTY, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> DYNASTY_BOOTS =
            ITEMS.registerItem("dynasty_boots", p -> new Item(p.humanoidArmor(ModArmorMaterials.DYNASTY, ArmorType.BOOTS)));

    // ================= ARMOR: IMPERIUM =================
    public static final DeferredItem<Item> IMPERIUM_HELMET =
            ITEMS.registerItem("imperium_helmet", props -> {
                Item.Properties p = props.humanoidArmor(ModArmorMaterials.IMPERIUM, ArmorType.HELMET);
                Equippable headEquip = Equippable.builder(EquipmentSlot.HEAD)
                        .setEquipSound(SoundEvents.ARMOR_EQUIP_CHAIN)
                        .setDamageOnHurt(true)
                        .build();
                return new Item(p.component(DataComponents.EQUIPPABLE, headEquip));
            });
    public static final DeferredItem<Item> IMPERIUM_CHESTPLATE =
            ITEMS.registerItem("imperium_chestplate", p -> new Item(p.humanoidArmor(ModArmorMaterials.IMPERIUM, ArmorType.CHESTPLATE)));
    public static final DeferredItem<Item> IMPERIUM_LEGGINGS =
            ITEMS.registerItem("imperium_leggings", p -> new Item(p.humanoidArmor(ModArmorMaterials.IMPERIUM, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> IMPERIUM_BOOTS =
            ITEMS.registerItem("imperium_boots", p -> new Item(p.humanoidArmor(ModArmorMaterials.IMPERIUM, ArmorType.BOOTS)));

    // ================= ARMOR: MISCHIEF =================
    public static final DeferredItem<Item> MISCHIEF_HELMET =
            ITEMS.registerItem("mischief_helmet", props -> {
                Item.Properties p = props.humanoidArmor(ModArmorMaterials.MISCHIEF, ArmorType.HELMET);
                Equippable headEquip = Equippable.builder(EquipmentSlot.HEAD)
                        .setEquipSound(SoundEvents.ARMOR_EQUIP_CHAIN)
                        .setDamageOnHurt(true)
                        .build();
                return new Item(p.component(DataComponents.EQUIPPABLE, headEquip));
            });
    public static final DeferredItem<Item> MISCHIEF_CHESTPLATE =
            ITEMS.registerItem("mischief_chestplate", p -> new Item(p.humanoidArmor(ModArmorMaterials.MISCHIEF, ArmorType.CHESTPLATE)));
    public static final DeferredItem<Item> MISCHIEF_LEGGINGS =
            ITEMS.registerItem("mischief_leggings", p -> new Item(p.humanoidArmor(ModArmorMaterials.MISCHIEF, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> MISCHIEF_BOOTS =
            ITEMS.registerItem("mischief_boots", p -> new Item(p.humanoidArmor(ModArmorMaterials.MISCHIEF, ArmorType.BOOTS)));


    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
