package net.anatomyworld.harambefmod.entity.mob.bananacow.clientmodel;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.entity.mob.bananacow.BananaCow;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public final class BananaCowRenderer
        extends MobRenderer<BananaCow, BananaCowModel.State, BananaCowModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HarambeCore.MOD_ID, "textures/entity/cow/banana_cow.png");

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
                                   @NotNull BananaCowModel.State s,
                                   float partialTick) {
        super.extractRenderState(entity, s, partialTick);

        // --- Smooth, correct “net head yaw” like 1.21.1 (and vanilla) ---
        // Interpolate body & head yaw, then compute relative yaw and wrap across ±180°
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYawDeg = Mth.wrapDegrees(headYaw - bodyYaw);

        // Interpolate pitch too
        float headPitchDeg = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        // (Optional) clamp for animals to avoid extreme snaps; tweak as you like
        netHeadYawDeg = Mth.clamp(netHeadYawDeg, -90.0F, 90.0F);
        headPitchDeg  = Mth.clamp(headPitchDeg,  -45.0F, 45.0F);

        // Store in radians for the model
        s.headYawRad   = netHeadYawDeg * Mth.DEG_TO_RAD;
        s.headPitchRad = headPitchDeg  * Mth.DEG_TO_RAD;

        // Walk anim with partial tick for smoothness
        s.limbSwing       = entity.walkAnimation.position(partialTick);
        s.limbSwingAmount = entity.walkAnimation.speed();

        // Age ticks for idle motions
        s.ageTicks = entity.tickCount + partialTick;
        // s.isBaby is already set by super.extractRenderState(entity, s, partialTick)
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BananaCowModel.State s) {
        return TEXTURE;
    }
}
