package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.CompletableFuture;

/** Recipes (1.21.8 style) */
public final class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    /** New 1.21.x signature: no args. Use this.output and protected helpers. */
    @Override
    protected void buildRecipes() {
        /* ---------- Shapeless ---------- */

        // RAW_ANYTOMITHIUM = iron + prismarine crystals + amethyst shard
        this.shapeless(RecipeCategory.MISC, ModItems.RAW_ANYTOMITHIUM.get())
                .requires(Items.RAW_IRON)
                .requires(Items.PRISMARINE_CRYSTALS)
                .requires(Items.AMETHYST_SHARD)
                .unlockedBy(getHasName(Items.AMETHYST_SHARD), this.has(Items.AMETHYST_SHARD))
                .save(this.output);

        // Flint and Pearl = flint + banana pearl
        this.shapeless(RecipeCategory.MISC, ModItems.FLINT_AND_PEARL.get())
                .requires(Items.FLINT)
                .requires(ModItems.BANANA_PEARL.get())
                .unlockedBy("has_flint", this.has(Items.FLINT))
                .unlockedBy("has_banana_pearl", this.has(ModItems.BANANA_PEARL.get()))
                .save(this.output);

        /* ---------- Smelting / Blasting ---------- */

        // Explicit ids (1.21.x uses ResourceKey<Recipe<?>>)
        ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> SMELT_KEY =
                ResourceKey.create(Registries.RECIPE,
                        ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "anytomithium_ingot_smelting"));

        ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> BLAST_KEY =
                ResourceKey.create(Registries.RECIPE,
                        ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "anytomithium_ingot_blasting"));

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModItems.RAW_ANYTOMITHIUM.get()),
                        RecipeCategory.MISC,
                        ModItems.ANYTOMITHIUM_INGOT.get(),
                        0.7F,
                        300)
                .unlockedBy("has_raw_anytomithium", this.has(ModItems.RAW_ANYTOMITHIUM.get()))
                .save(this.output, SMELT_KEY);

        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(ModItems.RAW_ANYTOMITHIUM.get()),
                        RecipeCategory.MISC,
                        ModItems.ANYTOMITHIUM_INGOT.get(),
                        0.7F,
                        150)
                .unlockedBy("has_raw_anytomithium", this.has(ModItems.RAW_ANYTOMITHIUM.get()))
                .save(this.output, BLAST_KEY);

        /* ---------- Shaped ---------- */

        this.shaped(RecipeCategory.MISC, ModItems.ANYPHONE.get())
                .pattern("xcx")
                .pattern("xgx")
                .pattern("xrx")
                .define('x', ModItems.ANYTOMITHIUM_INGOT.get())
                .define('c', Items.COMPASS)
                .define('g', Items.LIGHT_BLUE_STAINED_GLASS_PANE)
                .define('r', Items.REPEATER)
                .unlockedBy("has_anytomithium_ingot", this.has(ModItems.ANYTOMITHIUM_INGOT.get()))
                .save(this.output, ResourceKey.create(
                        Registries.RECIPE,
                        ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "anyphone")));
    }

    /** Runner hook for GatherDataEvent#createProvider(...) (1.21.x). */
    public static final class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }
        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "";
        }
    }
}
