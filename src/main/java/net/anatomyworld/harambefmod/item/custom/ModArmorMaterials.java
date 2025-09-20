// src/main/java/net/anatomyworld/harambefmod/item/custom/ModArmorMaterials.java
package net.anatomyworld.harambefmod.item.custom;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

// NEW 1.21.x equipment armor API
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;   // singular entry type
import net.minecraft.world.item.equipment.EquipmentAssets;  // registry (ROOT_ID)

import java.util.EnumMap;

public final class ModArmorMaterials {

    // Vanilla repair tag so anvil repairs behave
    private static final TagKey<Item> REPAIRS_LEATHER_ARMOR =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("minecraft", "repairs/leather_armor"));

    // Equipment asset ids → assets/<modid>/equipment/<name>.json
    public static final ResourceKey<EquipmentAsset> BELMONT_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "belmont"));
    public static final ResourceKey<EquipmentAsset> DYNASTY_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "dynasty"));
    public static final ResourceKey<EquipmentAsset> IMPERIUM_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "imperium"));
    public static final ResourceKey<EquipmentAsset> MISCHIEF_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "mischief"));

    // Leather-like defense: boots 1, legs 2, chest 3, helmet 1, body 0
    private static EnumMap<ArmorType, Integer> leatherDefense() {
        EnumMap<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.BOOTS, 1);
        map.put(ArmorType.LEGGINGS, 2);
        map.put(ArmorType.CHESTPLATE, 3);
        map.put(ArmorType.HELMET, 1);
        map.put(ArmorType.BODY, 0);
        return map;
    }

    // Weak, visual-oriented sets (leather-ish numbers per docs)
    public static final ArmorMaterial BELMONT = new ArmorMaterial(
            5,                      // durability multiplier (unit: HELMET 11, CHEST 16, LEGS 15, BOOTS 13, BODY 16)
            leatherDefense(),       // Map<ArmorType, Integer>
            15,                     // enchantability
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F,                   // toughness
            0.0F,                   // knockback resistance
            REPAIRS_LEATHER_ARMOR,  // repair tag
            BELMONT_ASSET           // equipment asset id (client render JSON)
    );

    public static final ArmorMaterial DYNASTY = new ArmorMaterial(
            5, leatherDefense(), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, REPAIRS_LEATHER_ARMOR, DYNASTY_ASSET
    );

    public static final ArmorMaterial IMPERIUM = new ArmorMaterial(
            5, leatherDefense(), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, REPAIRS_LEATHER_ARMOR, IMPERIUM_ASSET
    );

    public static final ArmorMaterial MISCHIEF = new ArmorMaterial(
            5, leatherDefense(), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, REPAIRS_LEATHER_ARMOR, MISCHIEF_ASSET
    );

    private ModArmorMaterials() {}
}
