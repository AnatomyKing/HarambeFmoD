package net.anatomyworld.harambefmod.data.genmodels;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant; // <-- correct import
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public final class ModelUtil {
    private ModelUtil() {}

    public static ResourceLocation idOf(Block b) {
        return BuiltInRegistries.BLOCK.getKey(b);
    }
    public static ResourceLocation rl(String ns, String path) {
        return ResourceLocation.fromNamespaceAndPath(ns, path);
    }
    public static ResourceLocation texOf(Block b) {
        var id = idOf(b);
        return rl(id.getNamespace(), "block/" + id.getPath());
    }
    public static ResourceLocation texOf(Block b, String suffix) {
        var id = idOf(b);
        return rl(id.getNamespace(), "block/" + id.getPath() + suffix);
    }
    public static ResourceLocation blockModel(String ns, String path) {
        return rl(ns, "block/" + path);
    }

    // Return the top-level MultiVariant (not a nested type).
    public static MultiVariant mv(ResourceLocation model) {
        return BlockModelGenerators.variants(new Variant(model));
    }



    public static Variant plain(ResourceLocation model) { return new Variant(model); }
}
