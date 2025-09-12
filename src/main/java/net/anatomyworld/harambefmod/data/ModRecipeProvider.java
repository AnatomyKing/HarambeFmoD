package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.data.recipegen.RecipeDSL;
import net.anatomyworld.harambefmod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.CompletableFuture;

public final class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        // Build the DSL with the required lookups + an Unlocker that forwards to our protected has(...)
        RecipeDSL dsl = new RecipeDSL(
                this.output,
                this.registries.lookupOrThrow(Registries.ITEM),
                this.registries.lookupOrThrow(Registries.TRIM_PATTERN),
                new RecipeDSL.Unlocker() {
                    @Override public net.minecraft.advancements.Criterion<?> item(net.minecraft.world.level.ItemLike i) { return ModRecipeProvider.this.has(i); }
                    @Override public net.minecraft.advancements.Criterion<?> tag(net.minecraft.tags.TagKey<net.minecraft.world.item.Item> t) { return ModRecipeProvider.this.has(t); }
                }
        );

        /* ---------- Shapeless ---------- */

        dsl.shapeless(RecipeCategory.MISC, ModItems.RAW_ANYTOMITHIUM.get())
                .requires(Items.RAW_IRON, Items.PRISMARINE_CRYSTALS, Items.AMETHYST_SHARD)
                .unlockedByHas(Items.AMETHYST_SHARD)
                .save("raw_anytomithium");

        dsl.shapeless(RecipeCategory.MISC, ModItems.FLINT_AND_PEARL.get())
                .requires(Items.FLINT, ModItems.BANANA_PEARL.get())
                .unlockedByHas(Items.FLINT, ModItems.BANANA_PEARL.get())
                .save("flint_and_pearl");

        dsl.shapeless(RecipeCategory.MISC, ModItems.HONEY_CRYSTALLINE.get(), 1)
                .requires(ModItems.BANANA_PEARL.get())
                .unlockedByHas(ModItems.BANANA_PEARL.get())
                .save("bananas_from_pearl");

        dsl.shapelessCountToCount(
                RecipeCategory.MISC,
                ModItems.HONEY_CRYSTALLINE.get(), 1,
                ModItems.CRYSTALLIZED_HONEY.get(), 9,
                "honey/crystallized_to_crystalline"
        );


        dsl.shapelessCountToCount(
                RecipeCategory.MISC,
                ModItems.HONEY_CLUSTER.get(), 1,
                ModItems.HONEY_CRYSTALLINE.get(), 9,
                "honey/crystalline_to_cluster"
        );


        dsl.shapelessCountToCount(
                RecipeCategory.MISC,
                ModItems.HONEY_CORE.get(), 1,
                ModItems.HONEY_CLUSTER.get(), 9,
                "honey/cluster_to_core"
        );


        /* ---------- Smelting / Blasting ---------- */

        ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> SMELT_KEY =
                ResourceKey.create(Registries.RECIPE,
                        ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "anytomithium_ingot_smelting"));
        ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> BLAST_KEY =
                ResourceKey.create(Registries.RECIPE,
                        ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "anytomithium_ingot_blasting"));

        dsl.cook().smelt(Ingredient.of(ModItems.RAW_ANYTOMITHIUM.get()),
                        RecipeCategory.MISC, ModItems.ANYTOMITHIUM_INGOT.get(), 0.7F, 300)
                .unlockedByHas(ModItems.RAW_ANYTOMITHIUM.get())
                .save(SMELT_KEY);

        dsl.cook().blast(Ingredient.of(ModItems.RAW_ANYTOMITHIUM.get()),
                        RecipeCategory.MISC, ModItems.ANYTOMITHIUM_INGOT.get(), 0.7F, 150)
                .unlockedByHas(ModItems.RAW_ANYTOMITHIUM.get())
                .save(BLAST_KEY);

        /* ---------- Shaped ---------- */

        dsl.shaped(RecipeCategory.MISC, ModItems.ANYPHONE.get())
                .pattern("xcx")
                .pattern("xgx")
                .pattern("xrx")
                .define('x', ModItems.ANYTOMITHIUM_INGOT.get())
                .define('c', Items.COMPASS)
                .define('g', Items.LIGHT_BLUE_STAINED_GLASS_PANE)
                .define('r', Items.REPEATER)
                .unlockedByHas(ModItems.ANYTOMITHIUM_INGOT.get())
                .save("anyphone");
    }

    /** Runner hook for GatherDataEvent#createProvider(...) (1.21.x). */
    public static final class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, java.util.concurrent.CompletableFuture<HolderLookup.Provider> registries) { super(output, registries); }
        @Override protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }
        @Override public String getName() { return "Recipes - " + HarambeCore.MOD_ID; }
    }
}
