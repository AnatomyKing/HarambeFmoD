package net.anatomyworld.harambefmod.entity.boat.musavacca.clientmodel;

import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.ResourceLocation;

public class MusavaccaBoatRenderer extends AbstractBoatRenderer {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "textures/entity/boat/musavacca.png");

    private final EntityModel<BoatRenderState> model;

    public MusavaccaBoatRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new MusavaccaBoatModel(ctx.bakeLayer(MusavaccaBoatModel.LAYER));
        this.shadowRadius = 0.8F; // vanilla-esque
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return this.model;
    }

    @Override
    protected RenderType renderType() {
        // Render layer that uses your texture
        return RenderType.entityCutoutNoCull(TEX);
    }
}
