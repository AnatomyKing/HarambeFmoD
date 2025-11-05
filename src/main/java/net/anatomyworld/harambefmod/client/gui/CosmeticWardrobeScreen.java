package net.anatomyworld.harambefmod.client.gui;

import net.anatomyworld.harambefmod.component.ModDataComponents;
import net.anatomyworld.harambefmod.network.ArmorCosmeticSkinPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class CosmeticWardrobeScreen extends Screen {

    private static final int ROW_HEIGHT  = 28;
    private static final int ROW_START_Y = 52;

    public CosmeticWardrobeScreen() {
        super(Component.literal("Cosmetic Wardrobe"));
    }

    @Override
    protected void init() {
        clearWidgets();

        int centerX = this.width / 2;
        int buttonWidth  = 40;
        int buttonHeight = 18;

        // For each armor slot: two buttons: ON / OFF
        addSlotButtons(centerX, EquipmentSlot.HEAD,  0, buttonWidth, buttonHeight);
        addSlotButtons(centerX, EquipmentSlot.CHEST, 1, buttonWidth, buttonHeight);
        addSlotButtons(centerX, EquipmentSlot.LEGS,  2, buttonWidth, buttonHeight);
        addSlotButtons(centerX, EquipmentSlot.FEET,  3, buttonWidth, buttonHeight);

        super.init();
    }

    private void addSlotButtons(int centerX,
                                EquipmentSlot slot,
                                int rowIndex,
                                int buttonWidth,
                                int buttonHeight) {

        int y = ROW_START_Y + rowIndex * ROW_HEIGHT - buttonHeight / 2;
        int onX  = centerX + 80;
        int offX = onX + buttonWidth + 6;

        // ON button -> active = true
        addRenderableWidget(
                Button.builder(Component.literal("ON"), b -> setSlotActive(slot, true))
                        .pos(onX, y)
                        .size(buttonWidth, buttonHeight)
                        .build()
        );

        // OFF button -> active = false
        addRenderableWidget(
                Button.builder(Component.literal("OFF"), b -> setSlotActive(slot, false))
                        .pos(offX, y)
                        .size(buttonWidth, buttonHeight)
                        .build()
        );
    }

    private void setSlotActive(EquipmentSlot slot, boolean active) {
        ClientPacketDistributor.sendToServer(new ArmorCosmeticSkinPayload(slot, active));

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            String state = active ? "ON" : "OFF";
            mc.player.displayClientMessage(
                    Component.literal("Cosmetic skin " + state + " for " + slot.getName())
                            .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED),
                    true
            );
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(g);

        Minecraft mc = Minecraft.getInstance();
        int centerX = this.width / 2;

        g.drawCenteredString(this.font, "Cosmetic Armor Skins (visual only)", centerX, 12, 0xFFFFFF);
        g.drawCenteredString(
                this.font,
                "Each armor piece can have its own cosmetic_skin + active flag.",
                centerX,
                24,
                0xAAAAAA
        );
        g.drawCenteredString(
                this.font,
                "ON/OFF only toggles rendering; the stored skin is never lost.",
                centerX,
                36,
                0xAAAAAA
        );

        // Armor rows
        if (mc.player != null) {
            drawArmorRow(g, mc.player.getItemBySlot(EquipmentSlot.HEAD),  EquipmentSlot.HEAD,  0);
            drawArmorRow(g, mc.player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST, 1);
            drawArmorRow(g, mc.player.getItemBySlot(EquipmentSlot.LEGS),  EquipmentSlot.LEGS,  2);
            drawArmorRow(g, mc.player.getItemBySlot(EquipmentSlot.FEET),  EquipmentSlot.FEET,  3);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawArmorRow(GuiGraphics g, ItemStack stack, EquipmentSlot slot, int rowIndex) {
        int centerX = this.width / 2;
        int y = ROW_START_Y + rowIndex * ROW_HEIGHT;
        int leftX = centerX - 140;

        // Background bar
        g.fill(leftX - 4, y - 6, centerX + 190, y + 18, 0x55000000);

        String slotName = slot.getName().substring(0, 1).toUpperCase() + slot.getName().substring(1);

        if (stack.isEmpty()) {
            g.drawString(
                    this.font,
                    slotName + ": (empty)",
                    leftX,
                    y,
                    0x888888,
                    false
            );
            return;
        }

        g.renderItem(stack, leftX, y - 4);

        ResourceLocation skin = stack.get(ModDataComponents.COSMETIC_SKIN.get());
        Boolean active = stack.get(ModDataComponents.COSMETIC_SKIN_ACTIVE.get());

        String baseTxt = slotName + ": " + stack.getHoverName().getString();
        String skinTxt = "Skin: " + (skin != null ? skin.toString() : "(none)");
        String activeTxt;

        if (skin == null) {
            activeTxt = "Status: no skin defined";
        } else {
            boolean isOn = (active == null || active);
            activeTxt = "Status: " + (isOn ? "ON" : "OFF");
        }

        g.drawString(this.font, baseTxt, leftX + 20, y - 4, 0xFFFFFF, false);
        g.drawString(this.font, skinTxt, leftX + 20, y + 6, 0xCCCCCC, false);
        g.drawString(this.font, activeTxt, leftX + 20, y + 16, 0xAAAAAA, false);
    }
}
