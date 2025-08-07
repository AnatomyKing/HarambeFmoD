// src/main/java/net/anatomyworld/harambefd/client/gui/FireColorSelectionScreen.java
package net.anatomyworld.harambefmod.client.gui;

import org.lwjgl.glfw.GLFW;
import net.anatomyworld.harambefmod.component.ModDataComponents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

public class FireColorSelectionScreen extends Screen {
    private final ItemStack stack;
    private EditBox colorField;
    private Component errorMessage = null;

    public FireColorSelectionScreen(ItemStack stack) {
        super(Component.literal("Flame Color"));
        this.stack = stack;
    }

    @Override
    protected void init() {
        super.init();
        int w  = 120, h = 20;
        int cx = width  / 2;
        int cy = height / 2;

        // Prefill with stored string or default "#D5CD49"
        String stored = stack.getOrDefault(
                ModDataComponents.FLAME_COLOR.get(),
                "#D5CD49"
        );

        colorField = new EditBox(font,
                cx - w/2, cy - 10,
                w, h,
                Component.literal("#RRGGBB"));
        colorField.setValue(stored);
        colorField.setMaxLength(7);
        // allow letters A-F too
        colorField.setFilter(s -> s.isEmpty() || s.matches("^#?[0-9A-Fa-f]*$"));
        colorField.setFocused(true);
        colorField.setCanLoseFocus(false);
        setFocused(colorField);
        addRenderableWidget(colorField);

        addRenderableWidget(Button.builder(Component.literal("Apply"), b -> submit())
                .bounds(cx - 30, cy + 15, 60, 20).build());
    }

    private void submit() {
        String in = colorField.getValue().trim();
        if (in.startsWith("#")) in = in.substring(1);
        if (in.length() != 6 || !in.matches("[0-9A-Fa-f]{6}")) {
            errorMessage = Component.literal("Invalid hex!")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF5555)));
            return;
        }

        /* store locally so the player sees the colour right away */
        stack.set(ModDataComponents.FLAME_COLOR.get(), "#" + in.toUpperCase());

        /* <-- add this – instantly tells the server about the new hex string */
        net.neoforged.neoforge.network.PacketDistributor
                .sendToServer(new net.anatomyworld.harambefmod.network.SyncColorPayload(
                        "#" + in.toUpperCase()
                ));

        net.minecraft.client.Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(key, scancode, mods);
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics gg, int mx, int my, float pt) {
        renderBackground(gg, mx, my, pt);
        gg.drawCenteredString(font, "Enter Default Flame Color:", width/2, height/2 - 30, 0xFFFFFF);
        super.render(gg, mx, my, pt);
        if (errorMessage != null) {
            gg.drawCenteredString(font, errorMessage, width/2, height/2 + 40, 0xFF5555);
        }
    }
}
