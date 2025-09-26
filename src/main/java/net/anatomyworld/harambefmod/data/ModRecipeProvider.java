package net.anatomyworld.harambefmod.data;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
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
import net.minecraft.tags.ItemTags;
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


        dsl.shapeless(RecipeCategory.MISC, ModItems.BELMONT_BANNER_PATTERN.get())
                .requires(Items.PAPER, ModBlocks.BELMONT_PLANKS)
                .unlockedByHas(ModBlocks.BELMONT_PLANKS)
                .save("faction/belmont_banner_pattern");

// Dynasty Banner Pattern
        dsl.shapeless(RecipeCategory.MISC, ModItems.DYNASTY_BANNER_PATTERN.get())
                .requires(Items.PAPER, ModBlocks.DYNASTY_PLANKS)
                .unlockedByHas(ModBlocks.DYNASTY_PLANKS)
                .save("faction/dynasty_banner_pattern");

// Imperium Banner Pattern
        dsl.shapeless(RecipeCategory.MISC, ModItems.IMPERIUM_BANNER_PATTERN.get())
                .requires(Items.PAPER, ModBlocks.IMPERIUM_PLANKS)
                .unlockedByHas(ModBlocks.IMPERIUM_PLANKS)
                .save("faction/imperium_banner_pattern");

// Mischief Banner Pattern
        dsl.shapeless(RecipeCategory.MISC, ModItems.MISCHIEF_BANNER_PATTERN.get())
                .requires(Items.PAPER, ModBlocks.MISCHIEF_PLANKS)
                .unlockedByHas(ModBlocks.MISCHIEF_PLANKS)
                .save("faction/mischief_banner_pattern");

        dsl.shapeless(RecipeCategory.MISC, ModItems.FLINT_AND_PEARL.get())
                .requires(Items.FLINT, ModItems.BANANA_PEARL.get())
                .unlockedByHas(Items.FLINT, ModItems.BANANA_PEARL.get())
                .save("faction/banana/flint_and_pearl");



//        dsl.shapeless(RecipeCategory.MISC, ModItems.HONEY_CRYSTALLINE.get(), 1)
//                .requires(ModItems.BANANA_PEARL.get())
//                .unlockedByHas(ModItems.BANANA_PEARL.get())
//                .save("bananas_from_pearl");
//
//        dsl.shapelessCountToCount(
//                RecipeCategory.MISC,
//                ModItems.HONEY_CRYSTALLINE.get(), 1,
//                ModItems.CRYSTALLIZED_HONEY.get(), 9,
//                "honey/crystallized_to_crystalline"
//        );


        dsl.shapelessCountToCount(
                RecipeCategory.MISC,
                ModBlocks.BANANA_PEARL_BLOCK.get(), 1,
                ModItems.BANANA_PEARL.get(), 9,
                "banana/banana_pearl_to_block"
        );

        dsl.shapelessCountToCount(
                RecipeCategory.MISC,
                ModBlocks.MUSAVACCA_PLANKS.get(), 4,
                ModBlocks.MUSAVACCA_STEM.get(), 1,
                "musavacca/stem_to_plank"
        );

        dsl.shapelessCountToCount(
                RecipeCategory.MISC,
                ModBlocks.MUSAVACCA_PLANKS.get(), 4,
                ModBlocks.STRIPPED_MUSAVACCA_STEM.get(), 1,
                "musavacca/strip_stem_to_plank"
        );

        dsl.shapelessCountToCount(
                RecipeCategory.MISC,
                ModBlocks.MUSAVACCA_PSEUDOSTEM.get(), 3,
                ModBlocks.MUSAVACCA_STEM.get(), 4,
                "musavacca/stem_to_pseudo"
        );

        dsl.shapelessCountToCount(
                RecipeCategory.MISC,
                ModBlocks.BELMONT_PLANKS.get(), 4,
                ModBlocks.BELMONT_LOG.get(), 1,
                "faction/belmont_to_plank"
        );
        dsl.shapelessCountToCount(
                RecipeCategory.MISC,
                ModBlocks.DYNASTY_PLANKS.get(), 4,
                ModBlocks.DYNASTY_LOG.get(), 1,
                "faction/dynasty_to_plank"
        );

        dsl.shapelessCountToCount(
                RecipeCategory.MISC,
                ModBlocks.IMPERIUM_PLANKS, 4,
                ModBlocks.IMPERIUM_LOG.get(), 1,
                "faction/imperium_to_plank"
        );

        dsl.shapelessCountToCount(
                RecipeCategory.MISC,
                ModBlocks.MISCHIEF_PLANKS,4,
                ModBlocks.MISCHIEF_LOG.get(), 1,
                "faction/mischief_to_plank"
        );

        dsl.shapelessCountToCount(
                RecipeCategory.MISC,
                ModItems.BANANA_PEARL,9,
                ModBlocks.BANANA_PEARL_BLOCK.get(), 1,
                "banana/block_to_pearl"
        );



//        dsl.shapelessCountToCount(
//                RecipeCategory.MISC,
//                ModItems.HONEY_CORE.get(), 1,
//                ModItems.HONEY_CLUSTER.get(), 9,
//                "honey/cluster_to_core"
//        );


        /* ---------- Smelting / Blasting ---------- */

        ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> SMELT_KEY =
                ResourceKey.create(Registries.RECIPE,
                        ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "anytomithium_ingot_smelting"));
        ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> BLAST_KEY =
                ResourceKey.create(Registries.RECIPE,
                        ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "anytomithium_ingot_blasting"));

        dsl.cook().smelt(Ingredient.of(ModItems.RAW_ANYTOMITHIUM.get()),
                        RecipeCategory.MISC, ModItems.PURPISH_ANYTOMITHIUM_INGOT.get(), 0.7F, 300)
                .unlockedByHas(ModItems.RAW_ANYTOMITHIUM.get())
                .save(SMELT_KEY);

        dsl.cook().blast(Ingredient.of(ModItems.RAW_ANYTOMITHIUM.get()),
                        RecipeCategory.MISC, ModItems.TEALISH_ANYTOMITHIUM_INGOT.get(), 0.7F, 150)
                .unlockedByHas(ModItems.RAW_ANYTOMITHIUM.get())
               .save(BLAST_KEY);

        /* ---------- Shaped ---------- */

        dsl.shaped(RecipeCategory.MISC, ModItems.ANYPHONE.get())
                .pattern("ttr")
                .pattern("pgp")
                .pattern("pcp")
                .define('p', ModItems.PURPISH_ANYTOMITHIUM_INGOT.get())
                .define('t', ModItems.TEALISH_ANYTOMITHIUM_INGOT.get())
                .define('c', Items.COMPASS)
                .define('g', Items.LIGHT_BLUE_STAINED_GLASS_PANE)
                .define('r', Items.REPEATER)
                .unlockedByHas(ModItems.PURPISH_ANYTOMITHIUM_INGOT.get())
                .save("anyphone");


        dsl.shaped(RecipeCategory.MISC, ModItems.BANANA_PEARL.get())
                .pattern("xxx")
                .pattern("xox")
                .pattern("xxx")
                .define('x', Items.AMETHYST_SHARD)
                .define('o', ModItems.BANANA)
                .unlockedByHas(ModItems.BANANA.get())
                .save("banana/banana_to_pearl");

        // Big Belmont Banner
// Big Belmont Banner
        dsl.shaped(RecipeCategory.DECORATIONS, ModBlocks.BIG_BELMONT_BANNER.get())
                .pattern("SFS")
                .pattern("WBW")
                .pattern("WMW")
                .define('S', ItemTags.WOODEN_SLABS)      // any wooden slab
                .define('F', ItemTags.WOODEN_FENCES)     // any wooden fence
                .define('W', ItemTags.WOOL)              // any wool
                .define('B', ItemTags.BANNERS)           // any banner
                .define('M', ModItems.BELMONT_BANNER_PATTERN.get())
                .unlockedByHas(ModItems.BELMONT_BANNER_PATTERN.get())
                .save("faction/big_belmont_banner");

// Big Dynasty Banner
        dsl.shaped(RecipeCategory.DECORATIONS, ModBlocks.BIG_DYNASTY_BANNER.get())
                .pattern("SFS")
                .pattern("WBW")
                .pattern("WMW")
                .define('S', ItemTags.WOODEN_SLABS)
                .define('F', ItemTags.WOODEN_FENCES)
                .define('W', ItemTags.WOOL)
                .define('B', ItemTags.BANNERS)
                .define('M', ModItems.DYNASTY_BANNER_PATTERN.get())
                .unlockedByHas(ModItems.DYNASTY_BANNER_PATTERN.get())
                .save("faction/big_dynasty_banner");

// Big Imperium Banner
        dsl.shaped(RecipeCategory.DECORATIONS, ModBlocks.BIG_IMPERIUM_BANNER.get())
                .pattern("SFS")
                .pattern("WBW")
                .pattern("WMW")
                .define('S', ItemTags.WOODEN_SLABS)
                .define('F', ItemTags.WOODEN_FENCES)
                .define('W', ItemTags.WOOL)
                .define('B', ItemTags.BANNERS)
                .define('M', ModItems.IMPERIUM_BANNER_PATTERN.get())
                .unlockedByHas(ModItems.IMPERIUM_BANNER_PATTERN.get())
                .save("faction/big_imperium_banner");

// Big Mischief Banner
        dsl.shaped(RecipeCategory.DECORATIONS, ModBlocks.BIG_MISCHIEF_BANNER.get())
                .pattern("SFS")
                .pattern("WBW")
                .pattern("WMW")
                .define('S', ItemTags.WOODEN_SLABS)
                .define('F', ItemTags.WOODEN_FENCES)
                .define('W', ItemTags.WOOL)
                .define('B', ItemTags.BANNERS)
                .define('M', ModItems.MISCHIEF_BANNER_PATTERN.get())
                .unlockedByHas(ModItems.MISCHIEF_BANNER_PATTERN.get())
                .save("faction/big_mischief_banner");




        dsl.shaped(RecipeCategory.MISC, ModBlocks.PEARLIDIAN.get(), 2)
                .pattern("xxx")
                .pattern("xox")
                .pattern("xxx")
                .define('x', ModBlocks.BANANA_PEARL_BLOCK.get())
                .define('o', Items.OBSIDIAN)
                .unlockedByHas(ModBlocks.BANANA_PEARL_BLOCK.get())
                .save("banana/pearlidian");
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
