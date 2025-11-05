package net.anatomyworld.harambefmod.client.render;

import net.anatomyworld.harambefmod.component.ModDataComponents;
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

    /**
     * For each armor slot we decide:
     *  - If that armor stack has harambefmod:cosmetic_skin AND cosmetic_skin_active != false
     *    → overlay that skin item.
     *  - Else → no overlay (EMPTY); vanilla armor renders normally underneath.
     */
    public static void onRegisterStateMods(RegisterRenderStateModifiersEvent e) {
        e.registerEntityModifier(
                net.minecraft.client.renderer.entity.player.PlayerRenderer.class,
                (AbstractClientPlayer player,
                 net.minecraft.client.renderer.entity.state.PlayerRenderState state) -> {

                    state.setRenderData(COS_HEAD,  skinStackFor(player, EquipmentSlot.HEAD));
                    state.setRenderData(COS_CHEST, skinStackFor(player, EquipmentSlot.CHEST));
                    state.setRenderData(COS_LEGS,  skinStackFor(player, EquipmentSlot.LEGS));
                    state.setRenderData(COS_FEET,  skinStackFor(player, EquipmentSlot.FEET));
                }
        );
    }

    private static ItemStack skinStackFor(AbstractClientPlayer player, EquipmentSlot slot) {
        ItemStack real = player.getItemBySlot(slot);
        if (real.isEmpty()) return ItemStack.EMPTY;

        ResourceLocation skinId = real.get(ModDataComponents.COSMETIC_SKIN.get());
        if (skinId == null) {
            // No cosmetic_skin component -> no overlay
            return ItemStack.EMPTY;
        }

        Boolean active = real.get(ModDataComponents.COSMETIC_SKIN_ACTIVE.get());
        // If explicitly false -> overlay OFF but skin remembered
        if (active != null && !active) {
            return ItemStack.EMPTY;
        }

        return stackForSkin(skinId, slot);
    }

    private static ItemStack stackForSkin(ResourceLocation id, EquipmentSlot slot) {
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(item);
        Equippable eq = stack.get(DataComponents.EQUIPPABLE);

        // If the skin item is armor, its slot must match; otherwise we allow it (head items, blocks, etc).
        if (eq != null && eq.slot() != slot) {
            return ItemStack.EMPTY;
        }

        return stack;
    }
}
