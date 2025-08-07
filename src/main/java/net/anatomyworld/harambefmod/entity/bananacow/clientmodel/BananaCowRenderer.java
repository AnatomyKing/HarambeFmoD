package net.anatomyworld.harambefmod.entity.bananacow.clientmodel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.entity.bananacow.BananaCow;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer for Banana Cow.
 * – Calves are rendered at 0.5× scale (here)
 * – “Big-head” baby look is handled inside {@link BananaCowModel}.
 */
public final class BananaCowRenderer
        extends MobRenderer<BananaCow, BananaCowModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    HarambeCore.MOD_ID, "textures/entity/banana_cow.png");

    /** shadow radius for an adult */
    private static final float ADULT_SHADOW = 0.7F;

    public BananaCowRenderer(EntityRendererProvider.Context ctx) {
        super(ctx,
                new BananaCowModel(ctx.bakeLayer(BananaCowModel.LAYER_LOCATION)),
                ADULT_SHADOW);
    }

    /* ------------------------------------------------------------------ */
    /* Shrink entire model + shadow for calves                            */
    /* ------------------------------------------------------------------ */
    @Override
    protected void scale(@NotNull BananaCow cow,
                         @NotNull PoseStack ps,
                         float partialTick) {

        if (cow.isBaby()) {
            ps.scale(0.5F, 0.5F, 0.5F);           // half-size body
            this.shadowRadius = ADULT_SHADOW * 0.5F;
        } else {
            this.shadowRadius = ADULT_SHADOW;
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BananaCow entity) {
        return TEXTURE;
    }
}
