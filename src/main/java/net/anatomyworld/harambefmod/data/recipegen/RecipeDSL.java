package net.anatomyworld.harambefmod.data.recipegen;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.ItemLike;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Tiny, ergonomic wrappers for vanilla recipe builders on 1.21.6–1.21.8.
 *
 * Handles 1.21.x changes:
 *  - Shaped/Shapeless builders need a Items registry lookup as first param.
 *  - Smithing Transform requires result as Item; we accept ItemLike and pass .asItem().
 *  - Smithing Trim requires Holder<TrimPattern> or ResourceKey<TrimPattern>.
 *  - RecipeProvider#has(...) is protected → pass an Unlocker adapter from your provider.
 *
 * New: "Number → Number" Shapeless helpers:
 *   - shapelessCountToCount(...)
 *   - shapelessCounts(...) for multiple counted inputs
 *   - compressChain(...) quick pattern for 9→1 + 1→9
 */
public final class RecipeDSL {

    /** Adapter so we can call provider's protected has(...) from outside. */
    public interface Unlocker {
        Criterion<?> item(ItemLike itemLike);
        Criterion<?> tag(TagKey<Item> tag);
    }

    private final RecipeOutput out;
    private final HolderLookup.RegistryLookup<Item> items;               // registries.lookupOrThrow(Registries.ITEM)
    private final HolderLookup.RegistryLookup<TrimPattern> trimPatterns; // registries.lookupOrThrow(Registries.TRIM_PATTERN)
    private final Unlocker unlocker;

    public RecipeDSL(RecipeOutput out,
                     HolderLookup.RegistryLookup<Item> items,
                     HolderLookup.RegistryLookup<TrimPattern> trimPatterns,
                     Unlocker unlocker) {
        this.out = out;
        this.items = items;
        this.trimPatterns = trimPatterns;
        this.unlocker = unlocker;
    }

    /* ---------- ids ---------- */

    public static ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> id(String path) {
        return ResourceKey.create(Registries.RECIPE,
                ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, path));
    }

    /* ---------- Shapeless ---------- */

    public Shapeless shapeless(RecipeCategory cat, ItemLike result, int count) {
        return new Shapeless(ShapelessRecipeBuilder.shapeless(this.items, cat, result, count));
    }
    public Shapeless shapeless(RecipeCategory cat, ItemLike result) { return shapeless(cat, result, 1); }

    public final class Shapeless {
        private final ShapelessRecipeBuilder b;
        private Shapeless(ShapelessRecipeBuilder b) { this.b = b; }

        /** Add ingredients by ItemLike, TagKey<Item> or Ingredient (count=1 each). */
        public Shapeless requires(Object... ings) {
            forEachIngredient(ings, in -> addOnce(b, in));
            return this;
        }

        /** Add a single ingredient N times (works for ItemLike / TagKey<Item> / Ingredient). */
        public Shapeless requiresCount(Object ingredient, int count) {
            if (count <= 0) throw new IllegalArgumentException("count must be >= 1");
            if (count > 9)  throw new IllegalArgumentException("shapeless max grid size is 9 (got " + count + ")");
            for (int i = 0; i < count; i++) addOnce(b, ingredient);
            return this;
        }

        /** Add pairs like (ingredient, count, ingredient, count, ...) */
        public Shapeless requiresPairs(Object... pairs) {
            if ((pairs.length & 1) != 0) {
                throw new IllegalArgumentException("requiresPairs expects even number of args: (ingredient, count)*");
            }
            for (int i = 0; i < pairs.length; i += 2) {
                Object ing = pairs[i];
                Object cnt = pairs[i + 1];
                if (!(cnt instanceof Number n)) {
                    throw new IllegalArgumentException("Count must be a Number at index " + (i + 1));
                }
                requiresCount(ing, n.intValue());
            }
            return this;
        }

        public Shapeless group(String g) { b.group(g); return this; }

        /** Unlocks by items (all). */
        public Shapeless unlockedByHas(ItemLike... items) {
            for (ItemLike i : items) b.unlockedBy(hasName(i), unlocker.item(i));
            return this;
        }
        /** Unlocks by a tag. */
        public Shapeless unlockedByHas(TagKey<Item> tag) {
            b.unlockedBy("has_" + tag.location().getPath(), unlocker.tag(tag));
            return this;
        }
        /** Unlock automatically for each ingredient (ItemLike/Tag). */
        public Shapeless unlockedByInputs(Object... ings) {
            forEachIngredient(ings, in -> {
                if (in instanceof ItemLike il) b.unlockedBy(hasName(il), unlocker.item(il));
                else if (in instanceof TagKey<?> tk && ((TagKey<?>) tk).registry().equals(Registries.ITEM)) {
                    @SuppressWarnings("unchecked") TagKey<Item> t = (TagKey<Item>) tk;
                    b.unlockedBy("has_" + t.location().getPath(), unlocker.tag(t));
                }
            });
            return this;
        }

        public void save(String path) { b.save(out, id(path)); }
        public void save(ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key) { b.save(out, key); }
    }

    /* ---------- Shaped ---------- */

    public Shaped shaped(RecipeCategory cat, ItemLike result, int count) {
        return new Shaped(ShapedRecipeBuilder.shaped(this.items, cat, result, count));
    }
    public Shaped shaped(RecipeCategory cat, ItemLike result) { return shaped(cat, result, 1); }

    public final class Shaped {
        private final ShapedRecipeBuilder b;
        private Shaped(ShapedRecipeBuilder b) { this.b = b; }

        public Shaped pattern(String line) { b.pattern(line); return this; }

        /** Map a single char to ItemLike / TagKey<Item> / Ingredient. */
        public Shaped define(char key, Object ingredient) {
            Objects.requireNonNull(ingredient, "ingredient");
            if (ingredient instanceof ItemLike il) b.define(key, il);
            else if (ingredient instanceof TagKey<?> tk && ((TagKey<?>) tk).registry().equals(Registries.ITEM)) {
                @SuppressWarnings("unchecked") TagKey<Item> t = (TagKey<Item>) tk;
                b.define(key, t);
            } else if (ingredient instanceof Ingredient ing) b.define(key, ing);
            else throw new IllegalArgumentException("Unsupported ingredient for key '" + key + "': " + ingredient);
            return this;
        }

        public Shaped group(String g) { b.group(g); return this; }

        public Shaped unlockedByHas(ItemLike... items) {
            for (ItemLike i : items) b.unlockedBy(hasName(i), unlocker.item(i));
            return this;
        }
        public Shaped unlockedByHas(TagKey<Item> tag) {
            b.unlockedBy("has_" + tag.location().getPath(), unlocker.tag(tag));
            return this;
        }

        public void save(String path) { b.save(out, id(path)); }
        public void save(ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key) { b.save(out, key); }
    }

    /* ---------- Cooking (smelt/blast/smoke/campfire) ---------- */

    public Cooking cook() { return new Cooking(); }

    public final class Cooking {
        private SimpleCookingRecipeBuilder b;

        public Cooking smelt(Ingredient in, RecipeCategory cat, ItemLike outItem, float xp, int time) {
            b = SimpleCookingRecipeBuilder.smelting(in, cat, outItem, xp, time);
            return this;
        }
        public Cooking blast(Ingredient in, RecipeCategory cat, ItemLike outItem, float xp, int time) {
            b = SimpleCookingRecipeBuilder.blasting(in, cat, outItem, xp, time);
            return this;
        }
        public Cooking smoke(Ingredient in, RecipeCategory cat, ItemLike outItem, float xp, int time) {
            b = SimpleCookingRecipeBuilder.smoking(in, cat, outItem, xp, time);
            return this;
        }
        public Cooking campfire(Ingredient in, RecipeCategory cat, ItemLike outItem, float xp, int time) {
            b = SimpleCookingRecipeBuilder.campfireCooking(in, cat, outItem, xp, time);
            return this;
        }

        public Cooking unlockedByHas(ItemLike... items) {
            for (ItemLike i : items) b.unlockedBy(hasName(i), unlocker.item(i));
            return this;
        }
        public Cooking unlockedByHas(TagKey<Item> tag) {
            b.unlockedBy("has_" + tag.location().getPath(), unlocker.tag(tag));
            return this;
        }
        public Cooking group(String g) { b.group(g); return this; }

        public void save(String path) { require(); b.save(out, id(path)); }
        public void save(ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key) { require(); b.save(out, key); }
        private void require() { if (b == null) throw new IllegalStateException("No cooking recipe configured"); }
    }

    /* ---------- Stonecutting ---------- */

    public Stonecut stonecut() { return new Stonecut(); }

    public final class Stonecut {
        private SingleItemRecipeBuilder b;

        public Stonecut of(Ingredient in, RecipeCategory cat, ItemLike outItem, int count) {
            b = SingleItemRecipeBuilder.stonecutting(in, cat, outItem, count);
            return this;
        }
        public Stonecut unlockedByHas(ItemLike... items) {
            for (ItemLike i : items) b.unlockedBy(hasName(i), unlocker.item(i));
            return this;
        }
        public Stonecut unlockedByHas(TagKey<Item> tag) {
            b.unlockedBy("has_" + tag.location().getPath(), unlocker.tag(tag));
            return this;
        }
        public Stonecut group(String g) { b.group(g); return this; }

        public void save(String path) { require(); b.save(out, id(path)); }
        public void save(ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key) { require(); b.save(out, key); }
        private void require() { if (b == null) throw new IllegalStateException("No stonecut recipe configured"); }
    }

    /* ---------- Smithing: Transform & Trim ---------- */

    public SmithingTransform transform() { return new SmithingTransform(); }
    public SmithingTrim trim() { return new SmithingTrim(); }

    public final class SmithingTransform {
        private SmithingTransformRecipeBuilder b;

        /** 1.21.x: result is an Item; we accept ItemLike and pass .asItem(). */
        public SmithingTransform of(Ingredient template, Ingredient base, Ingredient addition,
                                    RecipeCategory cat, ItemLike result) {
            b = SmithingTransformRecipeBuilder.smithing(template, base, addition, cat, result.asItem());
            return this;
        }
        /** Smithing in 1.21.x uses .unlocks(...). */
        public SmithingTransform unlocksHas(ItemLike... items) {
            for (ItemLike i : items) b.unlocks(hasName(i), unlocker.item(i));
            return this;
        }
        public SmithingTransform unlocksHas(TagKey<Item> tag) {
            b.unlocks("has_" + tag.location().getPath(), unlocker.tag(tag));
            return this;
        }
        public void save(String path) { require(); b.save(out, id(path)); }
        public void save(ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key) { require(); b.save(out, key); }
        private void require() { if (b == null) throw new IllegalStateException("No smithing transform configured"); }
    }

    public final class SmithingTrim {
        private SmithingTrimRecipeBuilder b;

        public SmithingTrim of(Ingredient template, Ingredient base, Ingredient addition,
                               Holder<TrimPattern> pattern, RecipeCategory cat) {
            b = SmithingTrimRecipeBuilder.smithingTrim(template, base, addition, pattern, cat);
            return this;
        }
        public SmithingTrim ofKey(Ingredient template, Ingredient base, Ingredient addition,
                                  ResourceKey<TrimPattern> patternKey, RecipeCategory cat) {
            Holder<TrimPattern> pattern = trimPatterns.getOrThrow(patternKey);
            return of(template, base, addition, pattern, cat);
        }
        public SmithingTrim unlocksHas(ItemLike... items) {
            for (ItemLike i : items) b.unlocks(hasName(i), unlocker.item(i));
            return this;
        }
        public SmithingTrim unlocksHas(TagKey<Item> tag) {
            b.unlocks("has_" + tag.location().getPath(), unlocker.tag(tag));
            return this;
        }
        public void save(String path) { require(); b.save(out, id(path)); }
        public void save(ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key) { require(); b.save(out, key); }
        private void require() { if (b == null) throw new IllegalStateException("No smithing trim configured"); }
    }

    /* ---------- Convenience helpers ---------- */

    /**
     * Simple "N of input → M of result" shapeless recipe.
     * Input can be ItemLike, TagKey&lt;Item&gt;, or Ingredient (then it repeats N times).
     * Example: shapelessCountToCount(MISC, out, 1, in, 9, "path/my_recipe");
     */
    public void shapelessCountToCount(RecipeCategory cat,
                                      ItemLike result, int resultCount,
                                      Object input, int inputCount,
                                      String path) {
        if (inputCount <= 0 || inputCount > 9)
            throw new IllegalArgumentException("inputCount must be in [1,9]");
        var s = shapeless(cat, result, resultCount);
        s.requiresCount(input, inputCount).unlockedByInputs(input).save(path);
    }

    /**
     * Multi-ingredient counted shapeless:
     *   pairs = (ingredient, count, ingredient, count, ...)
     * Example:
     *   shapelessCounts(MISC, result, 5, "mix/x",
     *     ModItems.A.get(), 4,
     *     ModItems.B.get(), 2
     *   );
     */
    public void shapelessCounts(RecipeCategory cat,
                                ItemLike result, int resultCount,
                                String path,
                                Object... ingredientCountPairs) {
        var s = shapeless(cat, result, resultCount);
        s.requiresPairs(ingredientCountPairs).unlockedByInputs(ingredientCountPairs).save(path);
    }

    /** 9 items → 1 block AND 1 block → 9 items. */
    public void compressChain(ItemLike item, ItemLike block,
                              RecipeCategory itemCat, RecipeCategory blockCat,
                              String packPrefix) {
        // 9 → 1
        shaped(blockCat, block)
                .pattern("xxx").pattern("xxx").pattern("xxx")
                .define('x', item)
                .unlockedByHas(item)
                .save(packPrefix + "/" + key(block));
        // 1 → 9
        shapeless(itemCat, item, 9)
                .requires(block)
                .unlockedByHas(block)
                .save(packPrefix + "/" + key(item) + "_from_block");
    }

    /* ---------- internal utils ---------- */

    private static void forEachIngredient(Object[] arr, Consumer<Object> fn) {
        for (Object o : arr) fn.accept(o);
    }

    private static void addOnce(ShapelessRecipeBuilder b, Object ingredient) {
        if (ingredient instanceof ItemLike il) b.requires(il);
        else if (ingredient instanceof TagKey<?> tk && ((TagKey<?>) tk).registry().equals(Registries.ITEM)) {
            @SuppressWarnings("unchecked") TagKey<Item> t = (TagKey<Item>) tk;
            b.requires(t);
        } else if (ingredient instanceof Ingredient ing) b.requires(ing);
        else throw new IllegalArgumentException("Unsupported ingredient: " + ingredient);
    }

    public static String key(ItemLike il) {
        ResourceLocation id = ResourceLocation.parse(
                net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(il.asItem()).toString()
        );
        return id.getPath();
    }
    private static String hasName(ItemLike i) { return "has_" + key(i); }
}
