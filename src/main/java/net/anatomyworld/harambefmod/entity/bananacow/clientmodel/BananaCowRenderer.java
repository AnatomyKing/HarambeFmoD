package net.anatomyworld.harambefmod.entity.bananacow.clientmodel;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.entity.bananacow.BananaCow;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class BananaCowRenderer
        extends MobRenderer<BananaCow, BananaCowModel.State, BananaCowModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "textures/entity/banana_cow.png");

    private static final float SHADOW = 0.7F;

    public BananaCowRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new BananaCowModel(ctx.bakeLayer(BananaCowModel.LAYER_LOCATION)), SHADOW);
    }

    @Override
    public @NotNull BananaCowModel.State createRenderState() {
        return new BananaCowModel.State();
    }

    @Override
    public void extractRenderState(@NotNull BananaCow entity,
                                   @NotNull BananaCowModel.State state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        // readable values for the model (no Yarn field names used)
        state.headYawDeg       = entity.getYHeadRot() - entity.getYRot();
        state.headPitchDeg     = entity.getXRot();
        state.limbSwing        = entity.walkAnimation.position();
        state.limbSwingAmount  = entity.walkAnimation.speed();
        state.ageTicks         = entity.tickCount + partialTick;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BananaCowModel.State state) {
        return TEXTURE;
    }
}
