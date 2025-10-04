package net.anatomyworld.harambefmod.client.gui;

import net.anatomyworld.harambefmod.menu.SimpleChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SimpleChestScreen extends AbstractContainerScreen<SimpleChestMenu> {
    // vanilla large chest background is 176×222 drawn from a 256×256 texture
    private static final int TEX_W = 256, TEX_H = 256;
    private static final int VANILLA_W = 176, VANILLA_H = 222;

    public SimpleChestScreen(SimpleChestMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = VANILLA_W;
        this.imageHeight = VANILLA_H;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96 + 2; // vanilla
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(
                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                this.menu.texture(), // dynamic texture from server
                this.leftPos, this.topPos,
                0f, 0f,
                VANILLA_W, VANILLA_H,
                TEX_W, TEX_H
        );
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override public boolean isPauseScreen() { return false; }
}
