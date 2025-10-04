package net.anatomyworld.harambefmod.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SimpleChestMenu extends AbstractContainerMenu {
    // 54 slots = 6 rows × 9 columns (large chest)
    private static final int ROWS = 6, COLS = 9, CHEST_SIZE = ROWS * COLS;

    /** Button slot -> diamond amount granted. */
    private static final Map<Integer, Integer> BUTTONS = Map.of(
            12, 1,
            3,  4,
            4,  16,
            5,  32,
            14, 64
    );
    private static final Set<Integer> BUTTON_INDEXES = new TreeSet<>(BUTTONS.keySet());

    /** These chest slots consume any items placed there (47..51 inclusive). */
    private static final Set<Integer> CONSUME_SLOTS = Set.of(47, 48, 49, 50, 51);

    private final Container container;
    private final ResourceLocation texture;

    // CLIENT ctor (from IMenuTypeExtension factory); reads texture sent by server.
    public SimpleChestMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, new SimpleContainer(CHEST_SIZE), buf.readResourceLocation());
    }

    // SERVER ctor (called when opening menu)
    public SimpleChestMenu(int containerId, Inventory playerInv, Container container, ResourceLocation texture) {
        super(ModMenus.SIMPLE_CHEST.get(), containerId);
        this.container = container;
        this.texture = texture;

        checkContainerSize(container, CHEST_SIZE);
        container.startOpen(playerInv.player);

        // --- Chest slots (6×9). Use special Slot types for buttons & consumption slots.
        int startX = 8, startY = 18, idx = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int x = startX + c * 18;
                int y = startY + r * 18;

                if (BUTTON_INDEXES.contains(idx)) {
                    this.addSlot(new IconSlot(container, idx, x, y)); // display-only buttons
                } else if (CONSUME_SLOTS.contains(idx)) {
                    this.addSlot(new ConsumeSlot(container, idx, x, y)); // eats anything placed here
                } else {
                    this.addSlot(new Slot(container, idx, x, y));
                }
                idx++;
            }
        }

        // Put diamonds with matching stack sizes in the button slots (server -> client sync)
        if (!playerInv.player.level().isClientSide) {
            for (var e : BUTTONS.entrySet()) {
                container.setItem(e.getKey(), new ItemStack(Items.DIAMOND, e.getValue()));
            }
            // Safety pass: ensure consume slots start empty
            clearConsumeSlots();
        }

        // --- Player inventory (3 rows) with 13px gap
        int invY = startY + ROWS * 18 + 13;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                this.addSlot(new Slot(playerInv, c + r * 9 + 9, startX + c * 18, invY + r * 18));
            }
        }

        // --- Hotbar
        int hotbarY = invY + 58;
        for (int c = 0; c < 9; c++) {
            this.addSlot(new Slot(playerInv, c, startX + c * 18, hotbarY));
        }
    }

    public ResourceLocation texture() { return texture; }

    @Override public boolean stillValid(Player player) { return true; }

    /** SHIFT-click handler with ALL button slots excluded. Consume slots are allowed (and will eat items). */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Shift-clicking a button slot: grant its amount and move nothing.
        if (BUTTON_INDEXES.contains(index)) {
            if (!player.level().isClientSide) grantDiamonds(player, BUTTONS.get(index));
            return ItemStack.EMPTY;
        }

        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack in = slot.getItem();
            result = in.copy();

            if (index < CHEST_SIZE) {
                // chest -> player
                if (!this.moveItemStackTo(in, CHEST_SIZE, CHEST_SIZE + 36, true)) return ItemStack.EMPTY;
            } else {
                // player -> chest (skip only button slots; consume slots remain valid targets)
                boolean moved = moveToChestSkippingButtons(in);
                if (!moved) return ItemStack.EMPTY;

                // extra safety (though ConsumeSlot#set already nukes contents)
                if (!player.level().isClientSide) clearConsumeSlots();
            }

            if (in.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
            if (in.getCount() == result.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, in);
        }
        return result;
    }

    /** Normal click on any button slot grants its amount and swallows the click. */
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (BUTTON_INDEXES.contains(slotId)) {
            if (!player.level().isClientSide) grantDiamonds(player, BUTTONS.get(slotId));
            return; // do not call super: prevents any movement/duplication
        }
        super.clicked(slotId, button, clickType, player);

        // Safety sweep: if anything somehow landed in consume slots (drag, modded behavior), delete it.
        if (!player.level().isClientSide) clearConsumeSlots();
    }

    /** Whenever the container changes (merges, drags, etc.), make sure consume slots are cleared. */
    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        clearConsumeSlots();
    }

    /** Block pick-all (double-click collect) and drag-painting into button slots only. Consume slots stay allowed. */
    @Override
    public boolean canTakeItemForPickAll(ItemStack carried, Slot slot) {
        if (slot != null && BUTTON_INDEXES.contains(slot.index)) return false;
        return super.canTakeItemForPickAll(carried, slot);
    }
    @Override
    public boolean canDragTo(Slot slot) {
        if (slot != null && BUTTON_INDEXES.contains(slot.index)) return false;
        return super.canDragTo(slot);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
        if (!player.level().isClientSide) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                // Don't drop the decorative diamonds or any leftover (should be none) in consume slots
                if (BUTTON_INDEXES.contains(i) || CONSUME_SLOTS.contains(i)) {
                    container.setItem(i, ItemStack.EMPTY);
                    continue;
                }
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty()) {
                    container.setItem(i, ItemStack.EMPTY);
                    if (!player.addItem(stack)) player.drop(stack, false);
                }
            }
        }
    }

    // --- helpers ---

    private static void grantDiamonds(Player player, int amount) {
        ItemStack gift = new ItemStack(Items.DIAMOND, amount);
        if (!player.addItem(gift)) player.drop(gift, false);
    }

    /** Try to move 'in' into chest slots 0..53, skipping only button indices (consume slots are valid targets). */
    private boolean moveToChestSkippingButtons(ItemStack in) {
        int start = 0;
        boolean moved = false;
        for (int forbidden : BUTTON_INDEXES) {
            if (start < forbidden && in.getCount() > 0) {
                moved |= this.moveItemStackTo(in, start, forbidden, false);
            }
            start = forbidden + 1;
            if (in.isEmpty()) break;
        }
        if (in.getCount() > 0 && start < CHEST_SIZE) {
            moved |= this.moveItemStackTo(in, start, CHEST_SIZE, false);
        }
        return moved;
    }

    /** Wipe the consume slots on the server side. */
    private void clearConsumeSlots() {
        if (this.container == null) return;
        for (int i : CONSUME_SLOTS) {
            ItemStack cur = container.getItem(i);
            if (!cur.isEmpty()) {
                container.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    /** Display-only slot: blocks place and pickup; shows a diamond stack. */
    private static class IconSlot extends Slot {
        public IconSlot(Container container, int index, int x, int y) { super(container, index, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
        @Override public int getMaxStackSize(ItemStack stack) { return 64; }
    }

    /**
     * Consuming slot: allows placement (so merges/shift-click target it),
     * but immediately deletes whatever is inserted and never lets you take.
     */
    private static class ConsumeSlot extends Slot {
        public ConsumeSlot(Container container, int index, int x, int y) { super(container, index, x, y); }

        @Override public boolean mayPlace(ItemStack stack) { return true; }     // allow anything to be placed
        @Override public boolean mayPickup(Player player) { return false; }     // nothing to take back

        @Override
        public void set(ItemStack stack) {
            // Delete anything that arrives, then mark empty.
            if (!stack.isEmpty()) {
                // Simply swallow the stack; don't forward it anywhere.
                super.set(ItemStack.EMPTY);
            } else {
                super.set(ItemStack.EMPTY);
            }
        }

        @Override public int getMaxStackSize(ItemStack stack) { return 64; }
    }
}
