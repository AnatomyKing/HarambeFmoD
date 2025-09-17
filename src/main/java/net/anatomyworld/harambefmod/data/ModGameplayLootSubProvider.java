// src/main/java/net/anatomyworld/harambefmod/data/ModGameplayLootSubProvider.java
package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.function.BiConsumer;

public final class ModGameplayLootSubProvider implements net.minecraft.data.loot.LootTableSubProvider {
    public static final ResourceKey<LootTable> SNIFFER_EXTRA =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "gameplay/sniffer_extra"));

    // 1.21.x: only this generate(...) exists — NO HolderLookup param.
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> out) {
        LootTable.Builder table = LootTable.lootTable()
                .setParamSet(LootContextParamSets.EMPTY)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(EmptyLootItem.emptyItem().setWeight(2)) // 2/3 nothing
                        .add(LootItem.lootTableItem(ModItems.MUSAVACCA_SPROUT.get()).setWeight(1))); // 1/3 sprout

        out.accept(SNIFFER_EXTRA, table);
    }
}
