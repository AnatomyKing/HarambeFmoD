// AnyChestMenu.java
package net.anatomyworld.harambefmod.menu;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AnyChestMenu extends AbstractContainerMenu {

    private static final int ROWS = 3;
    private static final int COLS = 9;
    private static final int CHEST_SIZE = ROWS * COLS;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int PLAYER_INV_SIZE = PLAYER_INV_ROWS * PLAYER_INV_COLS;
    private static final int HOTBAR_SIZE = 9;

    private final Container container;
    private final ContainerLevelAccess access;

    /**
     * CLIENT constructor – called via IMenuTypeExtension (id, inv, buf).
     * We don't need extra data, so we just ignore the buffer.
     */
    public AnyChestMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, new SimpleContainer(CHEST_SIZE), ContainerLevelAccess.NULL);
    }

    /**
     * SERVER constructor – used from AnyChestBlockEntity.
     */
    public AnyChestMenu(int containerId,
                        Inventory playerInv,
                        Container container,
                        ContainerLevelAccess access) {
        super(ModMenus.ANY_CHEST.get(), containerId);
        this.container = container;
        this.access = access;

        checkContainerSize(container, CHEST_SIZE);
        container.startOpen(playerInv.player);

        // --- AnyChest inventory: 3 rows × 9 cols ---
        int startX = 8;
        int startY = 18;
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                int x = startX + col * 18;
                int y = startY + row * 18;
                this.addSlot(new Slot(container, col + row * COLS, x, y));
            }
        }

        // --- Player inventory (3 rows) ---
        int invY = startY + ROWS * 18 + 13;
        for (int row = 0; row < PLAYER_INV_ROWS; ++row) {
            for (int col = 0; col < PLAYER_INV_COLS; ++col) {
                int x = startX + col * 18;
                int y = invY + row * 18;
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, x, y));
            }
        }

        // --- Hotbar ---
        int hotbarY = invY + 58;
        for (int col = 0; col < HOTBAR_SIZE; ++col) {
            int x = startX + col * 18;
            this.addSlot(new Slot(playerInv, col, x, hotbarY));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.ANYTOMITHIUM_CHEST.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack in = slot.getItem();
            result = in.copy();

            if (index < CHEST_SIZE) {
                // Chest → player inventory/hotbar
                if (!this.moveItemStackTo(in, CHEST_SIZE, CHEST_SIZE + PLAYER_INV_SIZE + HOTBAR_SIZE, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player inventory/hotbar → chest
                if (!this.moveItemStackTo(in, 0, CHEST_SIZE, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (in.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public Container getContainer() {
        return this.container;
    }
}
