package net.anatomyworld.harambefmod.client.gui;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.menu.AnyChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AnyChestScreen extends AbstractContainerScreen<AnyChestMenu> {

    // === Chest background (normal 3x9 chest, 176x166 inside 256x256) ===
    private static final int TEX_W = 256;
    private static final int TEX_H = 256;
    private static final int VANILLA_W = 176;
    private static final int VANILLA_H = 166;

    // assets/harambefmod/textures/gui/container/anytomithium_chest.png
    private static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    HarambeCore.MOD_ID,
                    "textures/gui/container/anytomithium_chest.png"
            );

    // === Top chest slot count (3x9) ===
    private static final int CHEST_SLOT_COUNT = 27;

    // === Custom digit textures (5x7 px each) ===
    // assets/harambefmod/textures/gui/container/numbers/0.png ... 9.png
    private static final int DIGIT_W = 5;      // width of each digit in pixels
    private static final int DIGIT_H = 7;      // height of each digit in pixels
    private static final int DIGIT_TEX_W = 5;  // texture width
    private static final int DIGIT_TEX_H = 7;  // texture height
    private static final int DIGIT_SPACING = DIGIT_W - 1; // 1px overlap between digits

    private static final ResourceLocation[] DIGIT_TEXTURES = new ResourceLocation[10];

    // Blue tint color #08DAFF with ~45% alpha
    private static final int BLUE_TINT = ARGB.color(
            115,      // alpha (0–255)
            0x08,     // red
            0xDA,     // green
            0xFF      // blue
    );

    static {
        for (int i = 0; i < 10; i++) {
            DIGIT_TEXTURES[i] = ResourceLocation.fromNamespaceAndPath(
                    HarambeCore.MOD_ID,
                    "textures/gui/container/numbers/" + i + ".png"
            );
        }
    }

    public AnyChestScreen(AnyChestMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = VANILLA_W;
        this.imageHeight = VANILLA_H;

        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96 + 2; // vanilla style
    }

    /* ===================== BACKGROUND ===================== */

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(
                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                BACKGROUND_TEXTURE,
                this.leftPos,
                this.topPos,
                0f,
                0f,
                VANILLA_W,
                VANILLA_H,
                TEX_W,
                TEX_H
        );
    }

    /* ===================== ITEM & NUMBERS ===================== */

    /**
     * Override ONLY the slot contents:
     * - Top chest slots (0..26): blue-tinted item + custom 5x7 digits.
     * - Bottom inventory / hotbar: pure vanilla via super.
     */
    @Override
    protected void renderSlotContents(GuiGraphics g, ItemStack stack, Slot slot, String altText) {
        // Determine if this slot is in the top chest part
        int slotIndex = this.menu.slots.indexOf(slot);
        boolean isTopChestSlot = slotIndex >= 0 && slotIndex < CHEST_SLOT_COUNT;

        // === Bottom GUI: vanilla rendering ===
        if (!isTopChestSlot) {
            super.renderSlotContents(g, stack, slot, altText);
            return;
        }

        // === Top GUI: custom behaviour ===
        if (stack.isEmpty()) {
            return;
        }

        // These coords are already in the chest GUI's local space
        int x = slot.x;
        int y = slot.y;

        // 1) Render the normal item sprite
        g.renderItem(stack, x, y);

        // 2) Blue tint overlay (16x16), under decorations & numbers
        g.fill(RenderPipelines.GUI, x, y, x + 16, y + 16, BLUE_TINT);

        // 3) Durability bar + cooldown, but NO vanilla stack count text
        g.renderItemDecorations(this.font, stack, x, y, "");

        // 4) Our custom 5x7 digit textures in bottom-right, with 1px overlap
        int count = stack.getCount();
        if (count <= 1) {
            return; // same rule as vanilla: no count if 1
        }

        String text = Integer.toString(count);
        int digits = text.length();
        if (digits <= 0) {
            return;
        }

        // Slot top-left in *this* GUI coordinate system (no leftPos added)
        int slotX = x;
        int slotY = y;

        // Total width with 1px overlap
        int totalWidth = DIGIT_W + (digits - 1) * DIGIT_SPACING;

        // Align bottom-right of a 16x16 icon
        int baseX = slotX + 16 - totalWidth;
        int baseY = slotY + 16 - DIGIT_H;

        for (int i = 0; i < digits; i++) {
            char ch = text.charAt(i);
            if (ch < '0' || ch > '9') continue;

            int digit = ch - '0';
            ResourceLocation tex = DIGIT_TEXTURES[digit];

            int dx = baseX + i * DIGIT_SPACING; // overlap by 1px
            int dy = baseY;

            g.blit(
                    RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                    tex,
                    dx,
                    dy,
                    0.0F,
                    0.0F,
                    DIGIT_W,
                    DIGIT_H,
                    DIGIT_TEX_W,
                    DIGIT_TEX_H
            );
        }
    }

    /* ===================== RENDER & PAUSE ===================== */

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
