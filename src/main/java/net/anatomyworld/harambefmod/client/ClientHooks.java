package net.anatomyworld.harambefmod.client;

import net.anatomyworld.harambefmod.client.gui.CosmeticWardrobeScreen;
import net.minecraft.client.Minecraft;

public final class ClientHooks {
    public static void openCosmeticWardrobeScreen() {
        Minecraft.getInstance().setScreen(new CosmeticWardrobeScreen());
    }
    private ClientHooks() {}
}
