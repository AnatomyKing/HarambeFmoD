// src/main/java/net/anatomyworld/harambefmod/data/ModModelProvider.java
package net.anatomyworld.harambefmod.data;

import com.google.gson.JsonObject;
import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.data.modelgen.BlocksGenComplex;
import net.anatomyworld.harambefmod.data.modelgen.BlocksGenSimple;
import net.anatomyworld.harambefmod.data.modelgen.ItemsGen;
import net.anatomyworld.harambefmod.item.ModItems;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Blocks;

import java.util.List;




public final class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) { super(output, HarambeCore.MOD_ID); }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {



        // 1) Simpele patronen
        BlocksGenSimple.cubeAll(blockModels,
                ModBlocks.BANANA_PEARL_BLOCK.get(),
                ModBlocks.MUSAVACCA_PLANKS.get(),
                ModBlocks.VANILLA_CREAM_STONE.get(),
                ModBlocks.BANANA_CREAM_STONE.get(),
                ModBlocks.CHOCO_CREAM_STONE.get(),
                ModBlocks.STRAWBERRY_CREAM_STONE.get(),
                ModBlocks.HONEY_CRYSTAL_BLOCK.get(),
                ModBlocks.PEARLIDIAN.get(),
                ModBlocks.BELMONT_PLANKS.get(),
                ModBlocks.DYNASTY_PLANKS.get(),
                ModBlocks.IMPERIUM_PLANKS.get(),
                ModBlocks.MISCHIEF_PLANKS.get(),
                ModBlocks.BELMONT_LEAVES.get(),
                ModBlocks.DYNASTY_LEAVES.get(),
                ModBlocks.IMPERIUM_LEAVES.get(),
                ModBlocks.MISCHIEF_LEAVES.get()
        );

        BlocksGenSimple.simpleState(blockModels, ModBlocks.MUSAVACCA_LEAVES_CROWN.get(),
                "harambefmod:block/musavacca_leaves_crown");
        BlocksGenSimple.simpleState(blockModels, ModBlocks.MUSAVACCA_LEAVES.get(),
                "harambefmod:block/musavacca_leaves");
        BlocksGenSimple.simpleState(blockModels, ModBlocks.MUSAVACCA_SAPLING.get(),
                "harambefmod:block/musavacca_plant_stage3");

        BlocksGenSimple.simpleState(blockModels, ModBlocks.ANYTOMITHIUM_CHEST.get(),
                "minecraft:block/chest");

        BlocksGenSimple.pillarAuto(blockModels,
                ModBlocks.MUSAVACCA_STEM.get(),
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get()
        );

        BlocksGenSimple.pillarAutoOverride(blockModels,
                ModBlocks.BELMONT_WOOD.get(),
                BlocksGenSimple.texOf(ModBlocks.BELMONT_LOG.get()),   // end
                BlocksGenSimple.texOf(ModBlocks.BELMONT_LOG.get())    // side
        );

        BlocksGenSimple.pillarAutoOverride(blockModels,
                ModBlocks.STRIPPED_BELMONT_WOOD.get(),
                BlocksGenSimple.texOf(ModBlocks.STRIPPED_BELMONT_LOG.get()), // end
                BlocksGenSimple.texOf(ModBlocks.STRIPPED_BELMONT_LOG.get())  // side
        );

        BlocksGenSimple.pillarAutoOverride(blockModels,
                ModBlocks.DYNASTY_WOOD.get(),
                BlocksGenSimple.texOf(ModBlocks.DYNASTY_LOG.get()),   // end
                BlocksGenSimple.texOf(ModBlocks.DYNASTY_LOG.get())    // side
        );

        BlocksGenSimple.pillarAutoOverride(blockModels,
                ModBlocks.STRIPPED_DYNASTY_WOOD.get(),
                BlocksGenSimple.texOf(ModBlocks.STRIPPED_DYNASTY_LOG.get()), // end
                BlocksGenSimple.texOf(ModBlocks.STRIPPED_DYNASTY_LOG.get())  // side
        );

        BlocksGenSimple.pillarAutoOverride(blockModels,
                ModBlocks.IMPERIUM_WOOD.get(),
                BlocksGenSimple.texOf(ModBlocks.IMPERIUM_LOG.get()),   // end
                BlocksGenSimple.texOf(ModBlocks.IMPERIUM_LOG.get())    // side
        );

        BlocksGenSimple.pillarAutoOverride(blockModels,
                ModBlocks.STRIPPED_IMPERIUM_WOOD.get(),
                BlocksGenSimple.texOf(ModBlocks.STRIPPED_IMPERIUM_LOG.get()), // end
                BlocksGenSimple.texOf(ModBlocks.STRIPPED_IMPERIUM_LOG.get())  // side
        );

        BlocksGenSimple.pillarAutoOverride(blockModels,
                ModBlocks.MISCHIEF_WOOD.get(),
                BlocksGenSimple.texOf(ModBlocks.MISCHIEF_LOG.get()),   // end
                BlocksGenSimple.texOf(ModBlocks.MISCHIEF_LOG.get())    // side
        );

        BlocksGenSimple.pillarAutoOverride(blockModels,
                ModBlocks.STRIPPED_MISCHIEF_WOOD.get(),
                BlocksGenSimple.texOf(ModBlocks.STRIPPED_MISCHIEF_LOG.get()), // end
                BlocksGenSimple.texOf(ModBlocks.STRIPPED_MISCHIEF_LOG.get())  // side
        );

        BlocksGenSimple.pillarAutoOverride(blockModels,
                ModBlocks.MUSAVACCA_PSEUDOSTEM.get(),
                BlocksGenSimple.texOf(ModBlocks.MUSAVACCA_STEM.get()),   // end
                BlocksGenSimple.texOf(ModBlocks.MUSAVACCA_STEM.get())    // side
        );

        BlocksGenSimple.pillarAutoOverride(blockModels,
                ModBlocks.STRIPPED_MUSAVACCA_PSEUDOSTEM.get(),
                BlocksGenSimple.texOf(ModBlocks.STRIPPED_MUSAVACCA_STEM.get()), // end
                BlocksGenSimple.texOf(ModBlocks.STRIPPED_MUSAVACCA_STEM.get())  // side
        );

        BlocksGenComplex.pillarNaturalCapAuto(blockModels,
                ModBlocks.DYNASTY_LOG.get(),
                ModBlocks.STRIPPED_DYNASTY_LOG.get()
        );

        BlocksGenComplex.pillarNaturalCapAuto(blockModels,
                ModBlocks.BELMONT_LOG.get(),
                ModBlocks.STRIPPED_BELMONT_LOG.get()
        );

        BlocksGenComplex.pillarNaturalCapAuto(blockModels,
                ModBlocks.IMPERIUM_LOG.get(),
                ModBlocks.STRIPPED_IMPERIUM_LOG.get()
        );

        BlocksGenComplex.pillarNaturalCapAuto(blockModels,
                ModBlocks.MISCHIEF_LOG.get(),
                ModBlocks.STRIPPED_MISCHIEF_LOG.get()
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


        BlocksGenComplex.catalystAuto(
                blockModels,
                ModBlocks.MISCHIEF_CATALYST.get(),
                BlocksGenSimple.texOf(ModBlocks.MISCHIEF_CATALYST.get(), "_side"),
                BlocksGenSimple.texOf(ModBlocks.MISCHIEF_CATALYST.get(), "_bottom"),
                BlocksGenSimple.texOf(ModBlocks.MISCHIEF_GRASS_BLOCK.get(), "_top"),
                BlocksGenSimple.texOf(ModBlocks.MISCHIEF_CATALYST.get(), "_side_bloom"),
                BlocksGenSimple.texOf(ModBlocks.MISCHIEF_CATALYST.get(), "_top_bloom")
        );

        BlocksGenComplex.veinMultiface(
                blockModels,
                ModBlocks.MISCHIEF_VEIN.get(),
                BlocksGenSimple.texOf(ModBlocks.MISCHIEF_VEIN.get()) // your vein texture
        );


        BlocksGenComplex.wallBannerScaled6Way(
                blockModels, itemModels,
                ModBlocks.BIG_BELMONT_BANNER.get(),
                ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "block/big_belmont_banner"),
                2f, 8f, 8f, 8f,
                /* wallTx */  -4.0f,
                /* wallTy */ -26.0f,
                /* wallTz */  -0.0f,
                /* upY   */  -8f,
                /* downY */  8f,
                true
        );

        BlocksGenComplex.wallBannerScaled6Way(
                blockModels, itemModels,
                ModBlocks.BIG_DYNASTY_BANNER.get(),
                ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "block/big_dynasty_banner"),
                2f, 8f, 8f, 8f,
                /* wallTx */  -4.0f,
                /* wallTy */ -26.0f,
                /* wallTz */  -0.0f,
                /* upY   */  -8f,
                /* downY */  8f,
                true
        );

        BlocksGenComplex.wallBannerScaled6Way(
                blockModels, itemModels,
                ModBlocks.BIG_IMPERIUM_BANNER.get(),
                ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "block/big_imperium_banner"),
                2f, 8f, 8f, 8f,
                /* wallTx */  -4.0f,
                /* wallTy */ -26.0f,
                /* wallTz */  -0.0f,
                /* upY   */  -8f,
                /* downY */  8f,
                true
        );

        BlocksGenComplex.wallBannerScaled6Way(
                blockModels, itemModels,
                ModBlocks.BIG_MISCHIEF_BANNER.get(),
                ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "block/big_mischief_banner"),
                2f, 8f, 8f, 8f,
                /* wallTx */  -4.0f,
                /* wallTy */ -26.0f,
                /* wallTz */  -0.0f,
                /* upY   */  -8f,
                /* downY */  8f,
                true
        );



        BlocksGenComplex.grassAutoNoOverlay(blockModels, ModBlocks.BELMONT_GRASS_BLOCK.get());
        BlocksGenComplex.grassAutoNoOverlay(blockModels, ModBlocks.DYNASTY_GRASS_BLOCK.get());
        BlocksGenComplex.grassAutoNoOverlay(blockModels, ModBlocks.IMPERIUM_GRASS_BLOCK.get());
        BlocksGenComplex.grassAutoNoOverlay(blockModels, ModBlocks.MISCHIEF_GRASS_BLOCK.get());

        BlocksGenSimple.cross(blockModels,
                ModBlocks.MUSAVACCA_FLOWER.get(),
                ModBlocks.CAROTENE_SHORT_GRASS.get(),
                ModBlocks.BELMONT_SHORT_GRASS.get(),
                ModBlocks.DYNASTY_SHORT_GRASS.get(),
                ModBlocks.IMPERIUM_SHORT_GRASS.get(),
                ModBlocks.MISCHIEF_SHORT_GRASS.get(),
                ModBlocks.BELMONT_SAPLING.get(),
                ModBlocks.DYNASTY_SAPLING.get(),
                ModBlocks.IMPERIUM_SAPLING.get(),
                ModBlocks.MISCHIEF_SAPLING.get()
        );

        // 2) Complex: portal axis copy van vanilla
        BlocksGenComplex.portalAxisStates(blockModels, ModBlocks.BANANA_PORTAL.get());

        // 3) Complex: custom FIRE (Pearl Fire) met vanilla FIRE templates
        BlocksGenComplex.fireStatesAuto(blockModels, ModBlocks.PEARL_FIRE.get());

        // 4) Jouw custom states
        BlocksGenComplex.bananaCowEggStates(blockModels, ModBlocks.BANANA_COW_EGG.get());

        BlocksGenComplex.musavaccaPlantStates(blockModels, ModBlocks.MUSAVACCA_PLANT.get());

        /* ===================== ITEMS ===================== */
        // Flat food/materials
        ItemsGen.flatItems(itemModels,
                ModItems.BANANA_PEARL.get(),
                ModItems.BANANA.get(),
                ModItems.PURPISH_ANYTOMITHIUM_INGOT.get(),
                ModItems.TEALISH_ANYTOMITHIUM_INGOT.get(),
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
                ModItems.HONEY_CORE.get(),
                ModItems.BELMONT_BANNER_PATTERN.get(),
                ModItems.DYNASTY_BANNER_PATTERN.get(),
                ModItems.IMPERIUM_BANNER_PATTERN.get(),
                ModItems.MISCHIEF_BANNER_PATTERN.get()
        );

        ItemsGen.flatBlockItems(blockModels,
                ModBlocks.SMALL_HONEY_CRYSTAL_BUD.get(),
                ModBlocks.MEDIUM_HONEY_CRYSTAL_BUD.get(),
                ModBlocks.LARGE_HONEY_CRYSTAL_BUD.get(),
                ModBlocks.HONEY_CRYSTAL_CLUSTER.get(),
                ModBlocks.MUSAVACCA_FLOWER.get(),
                ModBlocks.CAROTENE_SHORT_GRASS.get(),
                ModBlocks.BELMONT_SHORT_GRASS.get(),
                ModBlocks.DYNASTY_SHORT_GRASS.get(),
                ModBlocks.IMPERIUM_SHORT_GRASS.get(),
                ModBlocks.MISCHIEF_SHORT_GRASS.get(),
                ModBlocks.BELMONT_SAPLING.get(),
                ModBlocks.DYNASTY_SAPLING.get(),
                ModBlocks.IMPERIUM_SAPLING.get(),
                ModBlocks.MISCHIEF_SAPLING.get(),
                ModBlocks.MISCHIEF_VEIN.get()
        );

        ItemsGen.customItemModel(itemModels,
                ModItems.ANYPHONE.get(), "harambefmod:item/anyphone"
        );

        ItemsGen.itemUsesBlockModel(itemModels, ModBlocks.MUSAVACCA_LEAVES_CROWN.get().asItem(),
                "harambefmod:block/musavacca_leaves_crown");
        ItemsGen.itemUsesBlockModel(itemModels, ModBlocks.MUSAVACCA_LEAVES.get().asItem(),
                "harambefmod:block/musavacca_leaves");
        ItemsGen.itemUsesBlockModel(itemModels, ModBlocks.ANYTOMITHIUM_CHEST.get().asItem(),
                "harambefmod:item/anytomithium_chest_gui");
        ItemsGen.itemUsesBlockModel(itemModels, ModBlocks.MUSAVACCA_SAPLING.get().asItem(),
                "harambefmod:item/musavacca_sprout");

        BlocksGenComplex.amethystLikeClusterAuto(blockModels,
                ModBlocks.SMALL_HONEY_CRYSTAL_BUD.get(),
                ModBlocks.MEDIUM_HONEY_CRYSTAL_BUD.get(),
                ModBlocks.LARGE_HONEY_CRYSTAL_BUD.get(),
                ModBlocks.HONEY_CRYSTAL_CLUSTER.get());

        ItemsGen.flatItems(itemModels,
                ModItems.BELMONT_CHESTPLATE.get(), ModItems.BELMONT_LEGGINGS.get(), ModItems.BELMONT_BOOTS.get(),
                ModItems.DYNASTY_CHESTPLATE.get(), ModItems.DYNASTY_LEGGINGS.get(), ModItems.DYNASTY_BOOTS.get(),
                ModItems.IMPERIUM_CHESTPLATE.get(), ModItems.IMPERIUM_LEGGINGS.get(), ModItems.IMPERIUM_BOOTS.get(),
                ModItems.MISCHIEF_CHESTPLATE.get(), ModItems.MISCHIEF_LEGGINGS.get(), ModItems.MISCHIEF_BOOTS.get()
        );

// Helmets: selector + separate flat GUI model
        registerHelmetClientItem(itemModels, ModItems.BELMONT_HELMET.get(), "belmont");
        registerHelmetClientItem(itemModels, ModItems.DYNASTY_HELMET.get(), "dynasty");
        registerHelmetClientItem(itemModels, ModItems.IMPERIUM_HELMET.get(), "imperium");
        registerHelmetClientItem(itemModels, ModItems.MISCHIEF_HELMET.get(), "mischief");
    }


    private static void registerHelmetClientItem(ItemModelGenerators itemModels, Item helmet, String baseName) {
        // Item’s own model location (this is where the final “select” model will be written)
        ResourceLocation itemModelId = ModelLocationUtils.getModelLocation(helmet);

        // 1) Write a tiny flat (generated) submodel we’ll point the GUI case at.
        //    This is a separate json so the main item model can remain the select wrapper.
        var itemKey = BuiltInRegistries.ITEM.getKey(helmet);
        ResourceLocation flatId = ResourceLocation.fromNamespaceAndPath(
                itemKey.getNamespace(), "item/" + itemKey.getPath()
        );


        // layer0 -> textures/items/<helmet>.png (the usual place your flat icon lives)
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.LAYER0, ResourceLocation.fromNamespaceAndPath(itemKey.getNamespace(), "item/" + itemKey.getPath()));
        ModelTemplates.FLAT_ITEM.create(flatId, mapping, itemModels.modelOutput);

        // 2) Build “references” for the selector:
        //    - GUI -> the flat submodel we just created
        BlockModelWrapper.Unbaked guiModel = new BlockModelWrapper.Unbaked(flatId, List.of());
        //    - HEAD -> your 3D head model file
        BlockModelWrapper.Unbaked headModel = new BlockModelWrapper.Unbaked(
                ResourceLocation.fromNamespaceAndPath(itemKey.getNamespace(), "item/" + baseName + "_helmet_model"),
                List.of()
        );

        // 3) Save ONE model for the item which switches by display context.
        itemModels.itemModelOutput.accept(
                helmet,
                new SelectItemModel.Unbaked(
                        new SelectItemModel.UnbakedSwitch<>(
                                new DisplayContext(), // property to switch on
                                java.util.List.of(
                                        new SelectItemModel.SwitchCase<>(java.util.List.of(ItemDisplayContext.GUI),  guiModel),
                                        new SelectItemModel.SwitchCase<>(java.util.List.of(ItemDisplayContext.HEAD), headModel)
                                )
                        ),
                        java.util.Optional.of(guiModel) // fallback everywhere else
                )
        );
    }

    @Override
    public String getName() { return "Model Definitions - " + HarambeCore.MOD_ID; }
}
