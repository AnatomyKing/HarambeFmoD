// src/main/java/net/anatomyworld/harambefmod/data/ModGLMProvider.java
package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

public final class ModGLMProvider extends GlobalLootModifierProvider {
    // 1.21.x ctor requires the lookup future + modid
    public ModGLMProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, HarambeCore.MOD_ID);
    }

    @Override
    protected void start() {
        // Vanilla Sniffer loot table
        ResourceLocation VANILLA_SNIFFER = ResourceLocation.withDefaultNamespace("gameplay/sniffer_digging");
        // Our subtable as a ResourceKey<LootTable> (required by AddTableLootModifier)
        ResourceKey<LootTable> SUBTABLE = ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "gameplay/sniffer_extra")
        );

        LootItemCondition[] onlyWhenSnifferTable = new LootItemCondition[] {
                LootTableIdCondition.builder(VANILLA_SNIFFER).build()
        };

        add("sniffer_add_sprout", new AddTableLootModifier(onlyWhenSnifferTable, SUBTABLE));
    }
}
