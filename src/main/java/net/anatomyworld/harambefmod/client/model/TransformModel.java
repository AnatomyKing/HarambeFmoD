package net.anatomyworld.harambefmod.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.ComposedModelState;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import net.minecraft.client.resources.model.ModelDebugName;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.minecraft.client.resources.model.ResolvableModel;
import org.joml.Matrix4f;

import static net.anatomyworld.harambefmod.HarambeCore.MOD_ID;

/**
 * Loader that wraps another model and applies a root transform (scale/translate/pivot)
 * during baking. Works for blocks and items on NeoForge 1.21.8+.
 */
public final class TransformModel {

    /** loader id: harambefmod:transform */
    public static final ResourceLocation LOADER_ID =
            ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "transform");

    /** Unbaked wrapper returned by the loader */
    public static final class Unbaked extends AbstractUnbakedModel {
        private final Geometry geom;

        public Unbaked(StandardModelParameters params, Geometry geom) {
            super(params);
            this.geom = geom;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.geom.model());
        }

        @Override
        public ExtendedUnbakedGeometry geometry() {
            return this.geom;
        }
    }

    /** Geometry that bakes by delegating to the referenced model with an extra transform */
    public record Geometry(ResourceLocation model, Transformation extra)
            implements ExtendedUnbakedGeometry {

        @Override
        public QuadCollection bake(TextureSlots rootSlots,
                                   ModelBaker baker,
                                   ModelState incoming,
                                   ModelDebugName debugName,
                                   ContextMap context) {
            // Resolve the referenced model
            ResolvedModel resolved = baker.getModel(this.model);
            TextureSlots slots = resolved.getTopTextureSlots();

            // IMPORTANT: apply our transform FIRST, then the blockstate rotations.
            // This makes (tx,ty,tz) face-consistent and stops the texture “sliding”.
            ModelState combined = new ComposedModelState(incoming, this.extra);

            return resolved.bakeTopGeometry(slots, baker, combined);
        }
    }

    /** JSON loader */
    public static final class Loader implements UnbakedModelLoader<Unbaked> {
        @Override
        public Unbaked read(JsonObject json, JsonDeserializationContext ctx) {
            // Required: "model": "namespace:path"
            ResourceLocation delegate = ResourceLocation.parse(json.get("model").getAsString());

            float[] s = vec3(json, "scale",     1f, 1f, 1f);
            float[] t = vec3(json, "translate", 0f, 0f, 0f); // pixels
            float[] p = vec3(json, "pivot",     8f, 8f, 8f); // pixels

            // M = T(-pivot) * S * T(+pivot + translate/16)
            Matrix4f m = new Matrix4f().identity();
            m.translate(-p[0] / 16f, -p[1] / 16f, -p[2] / 16f);
            m.scale(s[0], s[1], s[2]);
            m.translate((p[0] + t[0]) / 16f, (p[1] + t[1]) / 16f, (p[2] + t[2]) / 16f);

            Transformation extra = new Transformation(m);

            // Include standard model params (ao, shade_quads, flip_v, emissive_ambient, transforms, etc.)
            StandardModelParameters params = StandardModelParameters.parse(json, ctx);

            return new Unbaked(params, new Geometry(delegate, extra));
        }

        private static float[] vec3(JsonObject j, String key, float dx, float dy, float dz) {
            if (!j.has(key)) return new float[]{dx, dy, dz};
            var a = j.getAsJsonArray(key);
            return new float[]{a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat()};
        }
    }
}