// src/main/java/net/anatomyworld/harambefmod/entity/boat/musavacca/clientmodel/MusavaccaBoatRenderer.java
package net.anatomyworld.harambefmod.entity.boat.musavacca.clientmodel;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** Vanilla-like renderer; physics-side lift handles height, so no custom water checks needed here. */
public class MusavaccaBoatRenderer extends AbstractBoatRenderer {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "textures/entity/boat/musavacca.png");

    private final EntityModel<BoatRenderState> model;

    public MusavaccaBoatRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new MusavaccaBoatModel(ctx.bakeLayer(MusavaccaBoatModel.LAYER));
        this.shadowRadius = 0.8F;
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return this.model;
    }

    @Override
    protected RenderType renderType() {
        return RenderType.entityCutoutNoCull(TEX);
    }

    public @NotNull ResourceLocation getTextureLocation(@NotNull BoatRenderState state) {
        return TEX;
    }
}
