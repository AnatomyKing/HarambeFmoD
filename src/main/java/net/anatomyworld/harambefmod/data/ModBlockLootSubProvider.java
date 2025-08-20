package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.custom.MusavaccaPlantCropBlock;
import net.anatomyworld.harambefmod.item.ModItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Set;
import java.util.stream.Collectors;

public final class ModBlockLootSubProvider extends BlockLootSubProvider {

    public ModBlockLootSubProvider(HolderLookup.Provider lookup) {
        super(Set.<Item>of(), FeatureFlags.DEFAULT_FLAGS, lookup);
    }

    @Override
    protected void generate() {
        // Normal blocks
        dropSelf(ModBlocks.BANANA_PEARL_BLOCK.get());
        dropSelf(ModBlocks.MUSAVACCA_PLANKS.get());
        dropSelf(ModBlocks.MUSAVACCA_STEM.get());
        dropSelf(ModBlocks.STRIPPED_MUSAVACCA_STEM.get());
        dropSelf(ModBlocks.MUSAVACCA_FLOWER.get());

        add(ModBlocks.MUSAVACCA_LEAVES.get(),
                createSilkTouchOrShearsDispatchTable(
                        ModBlocks.MUSAVACCA_LEAVES.get(),
                        net.minecraft.world.level.storage.loot.entries.EmptyLootItem.emptyItem()
                )
        );

        add(ModBlocks.MUSAVACCA_LEAVES_CROWN.get(),
                createSilkTouchOrShearsDispatchTable(
                        ModBlocks.MUSAVACCA_LEAVES_CROWN.get(),
                        net.minecraft.world.level.storage.loot.entries.EmptyLootItem.emptyItem()
                )
        );

        // --- Crop (age-based)
        var age0 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.MUSAVACCA_PLANT.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(MusavaccaPlantCropBlock.AGE, 0));
        var age1 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.MUSAVACCA_PLANT.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(MusavaccaPlantCropBlock.AGE, 1));
        var age2 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.MUSAVACCA_PLANT.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(MusavaccaPlantCropBlock.AGE, 2));

        add(ModBlocks.MUSAVACCA_PLANT.get(), LootTable.lootTable()
                // age 0 or 1 -> 1 seed
                .withPool(LootPool.lootPool()
                        .when(AnyOfCondition.anyOf(age0, age1))
                        .add(LootItem.lootTableItem(ModItems.MUSAVACCA_SPROUT.get())))
                // age 2 -> 1 seed + 1 stick
                .withPool(LootPool.lootPool()
                        .when(age2)
                        .add(LootItem.lootTableItem(ModItems.MUSAVACCA_SPROUT.get())))
                .withPool(LootPool.lootPool()
                        .when(age2)
                        .add(LootItem.lootTableItem(Items.STICK)))
        );

        // --- Sapling -> 1 seed + 3 sticks (never drops itself)
        add(ModBlocks.MUSAVACCA_SAPLING.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(ModItems.MUSAVACCA_SPROUT.get())))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(3))
                        .add(LootItem.lootTableItem(Items.STICK)))
        );

        // Egg uses custom spawnAfterBreak; keep empty to avoid double drops
        add(ModBlocks.BANANA_COW_EGG.get(), LootTable.lootTable());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(h -> (Block) h.value())
                .collect(Collectors.toList());
    }
}
