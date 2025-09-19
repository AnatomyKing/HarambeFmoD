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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
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
        var selfDropping = new Block[] {
                ModBlocks.BANANA_PEARL_BLOCK.get(),
                ModBlocks.BANANA_CREAM_STONE.get(),
                ModBlocks.VANILLA_CREAM_STONE.get(),
                ModBlocks.CHOCO_CREAM_STONE.get(),
                ModBlocks.STRAWBERRY_CREAM_STONE.get(),
                ModBlocks.MUSAVACCA_PLANKS.get(),
                ModBlocks.MUSAVACCA_STEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get(),
                ModBlocks.MUSAVACCA_FLOWER.get(),
                ModBlocks.PEARLIDIAN.get(),
                ModBlocks.ANYTOMITHIUM_CHEST.get(),
                ModBlocks.BELMONT_PLANKS.get(),
                ModBlocks.DYNASTY_PLANKS.get(),
                ModBlocks.IMPERIUM_PLANKS.get(),
                ModBlocks.MISCHIEF_PLANKS.get(),
                ModBlocks.STRIPPED_BELMONT_LOG.get(),
                ModBlocks.BELMONT_LOG.get(),
                ModBlocks.STRIPPED_DYNASTY_LOG.get(),
                ModBlocks.DYNASTY_LOG.get(),
                ModBlocks.STRIPPED_IMPERIUM_LOG.get(),
                ModBlocks.IMPERIUM_LOG.get(),
                ModBlocks.STRIPPED_MISCHIEF_LOG.get(),
                ModBlocks.MISCHIEF_LOG.get(),
                ModBlocks.MUSAVACCA_PSEUDOSTEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_PSEUDOSTEM.get(),
                ModBlocks.STRIPPED_BELMONT_WOOD.get(),
                ModBlocks.BELMONT_WOOD.get(),
                ModBlocks.STRIPPED_DYNASTY_WOOD.get(),
                ModBlocks.DYNASTY_WOOD.get(),
                ModBlocks.STRIPPED_IMPERIUM_WOOD.get(),
                ModBlocks.IMPERIUM_WOOD.get(),
                ModBlocks.STRIPPED_MISCHIEF_WOOD.get(),
                ModBlocks.MISCHIEF_WOOD.get(),
                ModBlocks.BELMONT_SAPLING.get(),
                ModBlocks.DYNASTY_SAPLING.get(),
                ModBlocks.IMPERIUM_SAPLING.get(),
                ModBlocks.MISCHIEF_SAPLING.get()
        };

        for (Block b : selfDropping) {
            dropSelf(b);
        }

        add(ModBlocks.MUSAVACCA_LEAVES.get(),
                createSilkTouchOrShearsDispatchTable(
                        ModBlocks.MUSAVACCA_LEAVES.get(),
                        net.minecraft.world.level.storage.loot.entries.EmptyLootItem.emptyItem()
                )
        );

        add(ModBlocks.BELMONT_LEAVES.get(),
                createSilkTouchOrShearsDispatchTable(
                        ModBlocks.BELMONT_LEAVES.get(),
                        net.minecraft.world.level.storage.loot.entries.EmptyLootItem.emptyItem()
                )
        );

        add(ModBlocks.DYNASTY_LEAVES.get(),
                createSilkTouchOrShearsDispatchTable(
                        ModBlocks.DYNASTY_LEAVES.get(),
                        net.minecraft.world.level.storage.loot.entries.EmptyLootItem.emptyItem()
                )
        );

        add(ModBlocks.IMPERIUM_LEAVES.get(),
                createSilkTouchOrShearsDispatchTable(
                        ModBlocks.IMPERIUM_LEAVES.get(),
                        net.minecraft.world.level.storage.loot.entries.EmptyLootItem.emptyItem()
                )
        );

        add(ModBlocks.MISCHIEF_LEAVES.get(),
                createSilkTouchOrShearsDispatchTable(
                        ModBlocks.MISCHIEF_LEAVES.get(),
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

        add(ModBlocks.BANANA_BLOCK.get(),
                createSilkTouchDispatchTable(
                        ModBlocks.BANANA_BLOCK.get(),
                        applyExplosionDecay(
                                ModBlocks.BANANA_BLOCK.get(),
                                LootItem.lootTableItem(ModItems.BANANA.get())
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(9)))
                        )
                )
        );

        add(ModBlocks.HONEY_CRYSTAL_BLOCK.get(),
                createSilkTouchDispatchTable(
                        ModBlocks.HONEY_CRYSTAL_BLOCK.get(),
                        applyExplosionDecay(
                                ModBlocks.HONEY_CRYSTAL_BLOCK.get(),
                                LootItem.lootTableItem(ModItems.HONEY_CLUSTER.get())
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4)))
                        )
                )
        );

        add(ModBlocks.HONEY_CRYSTAL_CLUSTER.get(),
                createSilkTouchDispatchTable(
                        ModBlocks.HONEY_CRYSTAL_CLUSTER.get(),
                        applyExplosionDecay(
                                ModBlocks.HONEY_CRYSTAL_BLOCK.get(),
                                LootItem.lootTableItem(ModItems.HONEY_CLUSTER.get())
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                        )
                )
        );

        add(ModBlocks.LARGE_HONEY_CRYSTAL_BUD.get(),
                createSilkTouchDispatchTable(
                        ModBlocks.LARGE_HONEY_CRYSTAL_BUD.get(),
                        applyExplosionDecay(
                                ModBlocks.LARGE_HONEY_CRYSTAL_BUD.get(),
                                LootItem.lootTableItem(ModItems.HONEY_CRYSTALLINE.get())
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4)))
                        )
                )
        );

        add(ModBlocks.MEDIUM_HONEY_CRYSTAL_BUD.get(),
                createSilkTouchDispatchTable(
                        ModBlocks.MEDIUM_HONEY_CRYSTAL_BUD.get(),
                        applyExplosionDecay(
                                ModBlocks.MEDIUM_HONEY_CRYSTAL_BUD.get(),
                                LootItem.lootTableItem(ModItems.HONEY_CRYSTALLINE.get())
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3)))
                        )
                )
        );

        add(ModBlocks.SMALL_HONEY_CRYSTAL_BUD.get(),
                createSilkTouchDispatchTable(
                        ModBlocks.SMALL_HONEY_CRYSTAL_BUD.get(),
                        applyExplosionDecay(
                                ModBlocks.SMALL_HONEY_CRYSTAL_BUD.get(),
                                LootItem.lootTableItem(ModItems.CRYSTALLIZED_HONEY.get())
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2)))
                        )
                )
        );

        add(ModBlocks.CAROTENE_GRASS_BLOCK.get(),
                createSilkTouchDispatchTable(
                        ModBlocks.CAROTENE_GRASS_BLOCK.get(),
                        LootItem.lootTableItem(Blocks.DIRT)
                )
        );

        add(ModBlocks.BELMONT_GRASS_BLOCK.get(),
                createSilkTouchDispatchTable(
                        ModBlocks.BELMONT_GRASS_BLOCK.get(),
                        LootItem.lootTableItem(Blocks.DIRT)
                )
        );

        add(ModBlocks.DYNASTY_GRASS_BLOCK.get(),
                createSilkTouchDispatchTable(
                        ModBlocks.DYNASTY_GRASS_BLOCK.get(),
                        LootItem.lootTableItem(Blocks.DIRT)
                )
        );

        add(ModBlocks.IMPERIUM_GRASS_BLOCK.get(),
                createSilkTouchDispatchTable(
                        ModBlocks.IMPERIUM_GRASS_BLOCK.get(),
                        LootItem.lootTableItem(Blocks.DIRT)
                )
        );


        add(ModBlocks.MISCHIEF_GRASS_BLOCK.get(),
                createSilkTouchDispatchTable(
                        ModBlocks.MISCHIEF_GRASS_BLOCK.get(),
                        LootItem.lootTableItem(Blocks.DIRT)
                )
        );

        // Egg uses custom spawnAfterBreak; keep empty to avoid double drops
        add(ModBlocks.BANANA_COW_EGG.get(), LootTable.lootTable());

        addShortGrassLoot(
                ModBlocks.CAROTENE_SHORT_GRASS.get(),
                ModBlocks.BELMONT_SHORT_GRASS.get(),
                ModBlocks.DYNASTY_SHORT_GRASS.get(),
                ModBlocks.IMPERIUM_SHORT_GRASS.get(),
                ModBlocks.MISCHIEF_SHORT_GRASS.get()
        );
    }


        private void addShortGrassLoot(Block... blocks) {
            var itemLookup = this.registries.lookupOrThrow(net.minecraft.core.registries.Registries.ITEM);
            var enchLookup = this.registries.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);

            for (Block b : blocks) {
                add(b, net.minecraft.world.level.storage.loot.LootTable.lootTable()
                        .withPool(net.minecraft.world.level.storage.loot.LootPool.lootPool()
                                .setRolls(net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly(1))
                                .add(net.minecraft.world.level.storage.loot.entries.AlternativesEntry.alternatives(
                                        // Shears -> drop itself
                                        net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem(b)
                                                .when(net.minecraft.world.level.storage.loot.predicates.MatchTool.toolMatches(
                                                        net.minecraft.advancements.critereon.ItemPredicate.Builder.item()
                                                                .of(itemLookup, net.minecraft.world.item.Items.SHEARS)
                                                )),

                                        // Otherwise -> 12.5% seeds, Fortune boosts (uniform bonus count *2), with explosion decay
                                        applyExplosionDecay(b,
                                                net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem(net.minecraft.world.item.Items.WHEAT_SEEDS)
                                                        .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance(0.125f))
                                                        .apply(net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.addUniformBonusCount(
                                                                enchLookup.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE), 2
                                                        ))
                                        )
                                ))
                        )
                );
            }
        }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(h -> (Block) h.value())
                .collect(Collectors.toList());
    }
}
