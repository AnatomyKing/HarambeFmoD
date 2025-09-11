package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.data.genmodels.BlocksGenComplex;
import net.anatomyworld.harambefmod.data.genmodels.BlocksGenSimple;
import net.anatomyworld.harambefmod.data.genmodels.ItemsGen;
import net.anatomyworld.harambefmod.item.ModItems;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;

public final class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) { super(output, HarambeCore.MOD_ID); }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        /* ===================== BLOKS ===================== */

        // 1) Simpele patronen
        BlocksGenSimple.cubeAll(blockModels,
                ModBlocks.BANANA_PEARL_BLOCK.get(),
                ModBlocks.MUSAVACCA_PLANKS.get()
        );

        BlocksGenSimple.leaves(blockModels,
                ModBlocks.MUSAVACCA_LEAVES.get(),
                ModBlocks.MUSAVACCA_LEAVES_CROWN.get()
        );

        BlocksGenSimple.pillarAuto(blockModels,
                ModBlocks.MUSAVACCA_STEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get()
        );

        // Planten/sapling (cross)
        BlocksGenSimple.cross(blockModels,
                ModBlocks.MUSAVACCA_FLOWER.get(),
                ModBlocks.MUSAVACCA_SAPLING.get()
        );

        // 2) Complex: portal axis copy van vanilla
        BlocksGenComplex.portalAxis(blockModels, ModBlocks.BANANA_PORTAL.get());

        // 3) Complex: custom FIRE (Pearl Fire) met vanilla FIRE templates
        BlocksGenComplex.pearlFire(blockModels, ModBlocks.PEARL_FIRE.get());

        // 4) Jouw custom states
        BlocksGenComplex.bananaCowEggStates(blockModels, ModBlocks.BANANA_COW_EGG.get());
        BlocksGenComplex.musavaccaCrop(blockModels, ModBlocks.MUSAVACCA_PLANT.get(), ModBlocks.MUSAVACCA_SAPLING.get());

        /* ===================== ITEMS ===================== */
        // Flat food/materials
        ItemsGen.flat(itemModels,
                ModItems.BANANA_PEARL.get(),
                ModItems.BANANA.get(),
                ModItems.ANYTOMITHIUM_INGOT.get(),
                ModItems.RAW_ANYTOMITHIUM.get(),
                ModItems.MUSAVACCA_BOAT_ITEM.get(),
                ModItems.FLINT_AND_PEARL.get(),
                ModItems.MUSAVACCA_SPROUT.get(),
                ModItems.BANANA_COW_EGG_RIPENING.get(),
                ModItems.BANANA_COW_EGG_RIPE.get(),
                ModItems.BANANA_COW_EGG_UNRIPE.get()
        );


        ItemsGen.customItemModel(itemModels,
                ModItems.ANYPHONE.get(), "harambefmod:item/anyphone"

        );

        ItemsGen.itemUsesBlockModel(itemModels,
                ModItems.MUSAVACCA_SPROUT.get(), "harambefmod:item/anyphone"

        );

    }

    @Override
    public String getName() { return "Model Definitions - " + HarambeCore.MOD_ID; }
}
