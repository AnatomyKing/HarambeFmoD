package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.data.modelgen.BlocksGenComplex;
import net.anatomyworld.harambefmod.data.modelgen.BlocksGenSimple;
import net.anatomyworld.harambefmod.data.modelgen.ItemsGen;
import net.anatomyworld.harambefmod.item.ModItems;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;

public final class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) { super(output, HarambeCore.MOD_ID); }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        /* ===================== BLOKS ===================== */

        // 1) Simpele patronen
        BlocksGenSimple.cubeAll(blockModels,
                ModBlocks.BANANA_PEARL_BLOCK.get(),
                ModBlocks.MUSAVACCA_PLANKS.get(),
                ModBlocks.VANILLA_CREAM_STONE.get(),
                ModBlocks.BANANA_CREAM_STONE.get(),
                ModBlocks.CHOCO_CREAM_STONE.get(),
                ModBlocks.STRAWBERRY_CREAM_STONE.get(),
                ModBlocks.HONEY_CRYSTAL_BLOCK.get(),
                ModBlocks.PEARLIDIAN.get()
        );

        BlocksGenSimple.simpleState(blockModels, ModBlocks.MUSAVACCA_LEAVES_CROWN.get(),
                "harambefmod:block/musavacca_leaves_crown");
        BlocksGenSimple.simpleState(blockModels, ModBlocks.MUSAVACCA_LEAVES.get(),
                "harambefmod:block/musavacca_leaves");
        BlocksGenSimple.simpleState(blockModels, ModBlocks.MUSAVACCA_SAPLING.get(),
                "harambefmod:block/musavacca_plant_stage3");


        BlocksGenSimple.pillarAuto(blockModels,
                ModBlocks.MUSAVACCA_STEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get()
        );

        BlocksGenSimple.barrelAutoTrivial(blockModels,
                ModBlocks.BANANA_BLOCK.get()
                );

        BlocksGenSimple.barrelAutoTrivialOverride(blockModels,
                ModBlocks.CAROTENE_GRASS_BLOCK.get(),
                BlocksGenSimple.texOf(ModBlocks.CAROTENE_GRASS_BLOCK.get()),        // side
                BlocksGenSimple.texOf(ModBlocks.CAROTENE_GRASS_BLOCK.get(), "_top"),// top
                BlocksGenSimple.texOf(Blocks.DIRT)  // bottom
        );



        BlocksGenSimple.cross(blockModels,
                ModBlocks.MUSAVACCA_FLOWER.get()
        );


        // 2) Complex: portal axis copy van vanilla
        BlocksGenComplex.portalAxisStates(blockModels, ModBlocks.BANANA_PORTAL.get());

        // 3) Complex: custom FIRE (Pearl Fire) met vanilla FIRE templates
        BlocksGenComplex.fireStatesAuto(blockModels, ModBlocks.PEARL_FIRE.get());

        // 4) Jouw custom states
        BlocksGenComplex.bananaCowEggStates(blockModels, ModBlocks.BANANA_COW_EGG.get());

        BlocksGenComplex.musavaccaPlantStates(blockModels,
                ModBlocks.MUSAVACCA_PLANT.get());

        /* ===================== ITEMS ===================== */
        // Flat food/materials
        ItemsGen.flatItems(itemModels,
                ModItems.BANANA_PEARL.get(),
                ModItems.BANANA.get(),
                ModItems.ANYTOMITHIUM_INGOT.get(),
                ModItems.RAW_ANYTOMITHIUM.get(),
                ModItems.MUSAVACCA_BOAT_ITEM.get(),
                ModItems.FLINT_AND_PEARL.get(),
                ModItems.MUSAVACCA_SPROUT.get(),
                ModItems.BANANA_COW_EGG_RIPENING.get(),
                ModItems.BANANA_COW_EGG_RIPE.get(),
                ModItems.BANANA_COW_EGG_UNRIPE.get(),
                ModItems.BANANA_COW_SPAWN_EGG.get(),
                ModItems.CRYSTALLIZED_HONEY.get(),
                ModItems.HONEY_CRYSTALLINE.get(),
                ModItems.HONEY_CLUSTER.get(),
                ModItems.HONEY_CORE.get()

        );

        ItemsGen.flatBlockItems(blockModels,
                ModBlocks.SMALL_HONEY_CRYSTAL_BUD.get(),
                ModBlocks.MEDIUM_HONEY_CRYSTAL_BUD.get(),
                ModBlocks.LARGE_HONEY_CRYSTAL_BUD.get(),
                ModBlocks.HONEY_CRYSTAL_CLUSTER.get(),
                ModBlocks.MUSAVACCA_FLOWER.get()
        );


        ItemsGen.customItemModel(itemModels,
                ModItems.ANYPHONE.get(), "harambefmod:item/anyphone"

        );

        ItemsGen.itemUsesBlockModel(
                itemModels,
                ModBlocks.MUSAVACCA_LEAVES_CROWN.get().asItem(),
                "harambefmod:block/musavacca_leaves_crown"
        );

        ItemsGen.itemUsesBlockModel(
                itemModels,
                ModBlocks.MUSAVACCA_LEAVES.get().asItem(),
                "harambefmod:block/musavacca_leaves"
        );

        ItemsGen.itemUsesBlockModel(
                itemModels,
                ModBlocks.MUSAVACCA_SAPLING.get().asItem(),
                "harambefmod:item/musavacca_sprout"
        );

        BlocksGenComplex.amethystLikeClusterAuto(blockModels,
                ModBlocks.SMALL_HONEY_CRYSTAL_BUD.get(),
                ModBlocks.MEDIUM_HONEY_CRYSTAL_BUD.get(),
                ModBlocks.LARGE_HONEY_CRYSTAL_BUD.get(),
                ModBlocks.HONEY_CRYSTAL_CLUSTER.get());


    }

    @Override
    public String getName() { return "Model Definitions - " + HarambeCore.MOD_ID; }
}
