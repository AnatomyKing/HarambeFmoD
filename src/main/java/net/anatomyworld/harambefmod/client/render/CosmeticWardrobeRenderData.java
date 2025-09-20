package net.anatomyworld.harambefmod.client.render;

import net.anatomyworld.harambefmod.attachment.ModAttachments;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

import javax.annotation.Nullable;

public final class CosmeticWardrobeRenderData {

    public static final ContextKey<ItemStack> COS_HEAD  =
            new ContextKey<>(ResourceLocation.fromNamespaceAndPath("harambefmod", "cos_head"));
    public static final ContextKey<ItemStack> COS_CHEST =
            new ContextKey<>(ResourceLocation.fromNamespaceAndPath("harambefmod", "cos_chest"));
    public static final ContextKey<ItemStack> COS_LEGS  =
            new ContextKey<>(ResourceLocation.fromNamespaceAndPath("harambefmod", "cos_legs"));
    public static final ContextKey<ItemStack> COS_FEET  =
            new ContextKey<>(ResourceLocation.fromNamespaceAndPath("harambefmod", "cos_feet"));

    private CosmeticWardrobeRenderData() {}

    /** MOD bus — push stacks into PlayerRenderState: cosmetic if present, else *real armor*. */
    public static void onRegisterStateMods(RegisterRenderStateModifiersEvent e) {
        e.registerEntityModifier(
                net.minecraft.client.renderer.entity.player.PlayerRenderer.class,
                (AbstractClientPlayer player, net.minecraft.client.renderer.entity.state.PlayerRenderState state) -> {
                    var w = player.getData(ModAttachments.COSMETIC_WARDROBE.get());
                    if (w == null) return;

                    state.setRenderData(COS_HEAD,  cosmeticOrReal(w.head(),  EquipmentSlot.HEAD,  player));
                    state.setRenderData(COS_CHEST, cosmeticOrReal(w.chest(), EquipmentSlot.CHEST, player));
                    state.setRenderData(COS_LEGS,  cosmeticOrReal(w.legs(),  EquipmentSlot.LEGS,  player));
                    state.setRenderData(COS_FEET,  cosmeticOrReal(w.feet(),  EquipmentSlot.FEET,  player));
                }
        );
    }

    /** If cosmetic id is present and valid for the slot, build that stack; else return the player's real armor. */
    private static ItemStack cosmeticOrReal(@Nullable ResourceLocation id, EquipmentSlot slot, AbstractClientPlayer player) {
        ItemStack cosmetic = stackFor(id, slot);
        return cosmetic.isEmpty() ? player.getItemBySlot(slot) : cosmetic;
    }

    /** Build an ItemStack for an id and ensure it’s equippable for the expected slot. */
    private static ItemStack stackFor(@Nullable ResourceLocation id, EquipmentSlot slot) {
        if (id == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(item);
        Equippable eq = stack.get(DataComponents.EQUIPPABLE);
        if (eq == null || eq.slot() != slot) return ItemStack.EMPTY;
        return stack; // EquipmentLayerRenderer reads client info (asset) from this stack.
    }
}
