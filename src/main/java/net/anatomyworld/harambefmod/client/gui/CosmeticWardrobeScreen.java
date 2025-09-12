package net.anatomyworld.harambefmod.client.gui;

import net.anatomyworld.harambefmod.cosmetic.CosmeticSet;
import net.anatomyworld.harambefmod.cosmetic.client.ClientCosmeticSets;
import net.anatomyworld.harambefmod.network.SelectCosmeticSetPayload;
import net.anatomyworld.harambefmod.network.ClearCosmeticWardrobePayload;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CosmeticWardrobeScreen extends Screen {
    private static final int COLS = 2;
    private static final int CELL_W = 180;
    private static final int CELL_H = 42;
    private static final int GAP = 8;
    private static final int TOP_PAD = 38;

    private List<ResourceLocation> order = new ArrayList<>();

    public CosmeticWardrobeScreen() {
        super(Component.literal("Cosmetic Sets"));
    }

    @Override
    protected void init() {
        order = new ArrayList<>(ClientCosmeticSets.sets().keySet());
        clearWidgets();

        // Global "Clear" button (top-right)
        int clearW = 60, clearH = 20;
        addRenderableWidget(
                Button.builder(Component.literal("Clear"), b -> clearAll())
                        .pos(this.width - clearW - 10, 8)
                        .size(clearW, clearH)
                        .build()
        );

        int gridW = COLS * CELL_W + (COLS - 1) * GAP;
        int x0 = (width - gridW) / 2;
        int y0 = TOP_PAD;

        int i = 0;
        for (ResourceLocation id : order) {
            int row = i / COLS, col = i % COLS;
            int x = x0 + col * (CELL_W + GAP);
            int y = y0 + row * (CELL_H + GAP);

            addRenderableWidget(
                    Button.builder(Component.literal("Equip"), b -> equip(id))
                            .pos(x + CELL_W - 60, y + (CELL_H - 20) / 2)
                            .size(56, 20)
                            .build()
            );
            i++;
        }

        super.init();
    }

    private void equip(ResourceLocation id) {
        ClientPacketDistributor.sendToServer(new SelectCosmeticSetPayload(id));
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("Equipped set: ").append(id.toString()).withStyle(ChatFormatting.YELLOW),
                    true
            );
        }
    }

    private void clearAll() {
        ClientPacketDistributor.sendToServer(ClearCosmeticWardrobePayload.INSTANCE);
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("Cleared cosmetic armor").withStyle(ChatFormatting.RED),
                    true
            );
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(g);
        g.drawCenteredString(this.font, "Cosmetic Armor (visual only)", this.width / 2, 12, 0xFFFFFF);

        Map<ResourceLocation, CosmeticSet> sets = ClientCosmeticSets.sets();

        if (order.isEmpty()) {
            g.drawCenteredString(this.font, "No cosmetic sets found", this.width / 2, this.height / 2 - 5, 0xAAAAAA);
            super.render(g, mouseX, mouseY, partialTick);
            return;
        }

        int gridW = COLS * CELL_W + (COLS - 1) * GAP;
        int x0 = (width - gridW) / 2;
        int y0 = TOP_PAD;

        for (int i = 0; i < order.size(); i++) {
            ResourceLocation id = order.get(i);
            CosmeticSet set = sets.get(id);
            if (set == null) continue;

            int row = i / COLS, col = i % COLS;
            int x = x0 + col * (CELL_W + GAP);
            int y = y0 + row * (CELL_H + GAP);

            g.fill(x, y, x + CELL_W, y + CELL_H, 0x66000000);
            g.fill(x, y, x + CELL_W, y + 1, 0x22FFFFFF);
            g.fill(x, y + CELL_H - 1, x + CELL_W, y + CELL_H, 0x22000000);
            g.fill(x, y, x + 1, y + CELL_H, 0x22FFFFFF);
            g.fill(x + CELL_W - 1, y, x + CELL_W, y + CELL_H, 0x22000000);

            String title = set.title().orElse(id.getPath());
            g.drawString(this.font, title, x + 8, y + 6, 0xFFFFFF, false);

            int iconX = x + 10, iconY = y + 18, step = 18;
            drawIcon(g, set.head().orElse(null),  iconX + step * 0, iconY);
            drawIcon(g, set.chest().orElse(null), iconX + step * 1, iconY);
            drawIcon(g, set.legs().orElse(null),  iconX + step * 2, iconY);
            drawIcon(g, set.feet().orElse(null),  iconX + step * 3, iconY);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawIcon(GuiGraphics g, ResourceLocation itemId, int x, int y) {
        if (itemId == null) return;
        var maybeItem = BuiltInRegistries.ITEM.getOptional(itemId);
        if (maybeItem.isEmpty()) return;
        Item item = maybeItem.get();
        g.renderItem(new ItemStack(item), x, y);
    }
}
