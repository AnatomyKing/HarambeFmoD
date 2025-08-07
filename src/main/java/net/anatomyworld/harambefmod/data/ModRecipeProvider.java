package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput out,
                             CompletableFuture<HolderLookup.Provider> lookup) {
        super(out, lookup);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput out) {

        /* RAW → shapeless */
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RAW_ANYTOMITHIUM.get())
                .requires(Items.RAW_IRON)
                .requires(Items.PRISMARINE_CRYSTALS)
                .requires(Items.AMETHYST_SHARD)
                .unlockedBy("has_amethyst", has(Items.AMETHYST_SHARD))
                .save(out);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.FLINT_AND_PEARL.get())
                .requires(Items.FLINT)
                .requires(ModItems.BANANA_PEARL.get())
                .unlockedBy("has_flint", has(Items.FLINT))
                .unlockedBy("has_banana_pearl", has(ModItems.BANANA_PEARL.get()))
                .save(out);

        /* SMELTING */
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModItems.RAW_ANYTOMITHIUM.get()),
                        RecipeCategory.MISC,
                        ModItems.ANYTOMITHIUM_INGOT.get(),
                        0.7F,
                        300)
                .unlockedBy("has_raw_anytomithium", has(ModItems.RAW_ANYTOMITHIUM.get()))
                .save(out, ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "anytomithium_ingot_smelting"));

        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(ModItems.RAW_ANYTOMITHIUM.get()),
                        RecipeCategory.MISC,
                        ModItems.ANYTOMITHIUM_INGOT.get(),
                        0.7F,
                        150)
                .unlockedBy("has_raw_anytomithium", has(ModItems.RAW_ANYTOMITHIUM.get()))
                .save(out, ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "anytomithium_ingot_blasting"));

        /* ANYPHONE */
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ANYPHONE.get())
                .pattern("xcx")
                .pattern("xgx")
                .pattern("xrx")
                .define('x', ModItems.ANYTOMITHIUM_INGOT.get())
                .define('c', Items.COMPASS)
                .define('g', Items.LIGHT_BLUE_STAINED_GLASS_PANE)
                .define('r', Items.REPEATER)
                .unlockedBy("has_anytomithium_ingot", has(ModItems.ANYTOMITHIUM_INGOT.get()))
                .save(out, ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "anyphone"));
    }
}
