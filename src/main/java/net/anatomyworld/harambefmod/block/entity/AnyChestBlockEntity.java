package net.anatomyworld.harambefmod.block.entity;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlockEntities;
import net.anatomyworld.harambefmod.menu.AnyChestMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class AnyChestBlockEntity extends BaseContainerBlockEntity implements LidBlockEntity {

    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);

    private final ChestLidController lid = new ChestLidController();

    private final ContainerOpenersCounter openers = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.playSound(
                    null,
                    pos,
                    SoundEvents.CHEST_OPEN,
                    SoundSource.BLOCKS,
                    0.5F,
                    0.9F + level.random.nextFloat() * 0.1F
            );
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.playSound(
                    null,
                    pos,
                    SoundEvents.CHEST_CLOSE,
                    SoundSource.BLOCKS,
                    0.5F,
                    0.9F + level.random.nextFloat() * 0.1F
            );
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
            AnyChestBlockEntity.this.signalOpenCount(level, pos, state, oldCount, newCount);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            if (!(player.containerMenu instanceof AnyChestMenu menu)) {
                return false;
            }
            return menu.getContainer() == AnyChestBlockEntity.this;
        }
    };

    public AnyChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANY_CHEST_ENTITY.get(), pos, state);
    }

    /* ---------------- inventory ---------------- */

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    /* --------- helpers to save/load contents on the chest item --------- */

    /**
     * Save this chest's contents into the given ItemStack
     * using the DataComponents.CONTAINER component.
     */
    public void saveToItem(ItemStack stack) {
        int size = this.getContainerSize();
        List<ItemStack> list = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            ItemStack slot = this.getItem(i);
            list.add(slot.isEmpty() ? ItemStack.EMPTY : slot.copy());
        }

        ItemContainerContents contents = ItemContainerContents.fromItems(list);
        if (contents != ItemContainerContents.EMPTY) {
            stack.set(DataComponents.CONTAINER, contents);
        }
    }

    /**
     * Load contents from the chest ItemStack (if present) into this block entity.
     */
    public void loadFromItem(ItemStack stack) {
        ItemContainerContents contents = stack.getOrDefault(
                DataComponents.CONTAINER,
                ItemContainerContents.EMPTY
        );
        if (contents == ItemContainerContents.EMPTY) {
            return;
        }

        NonNullList<ItemStack> newItems = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        contents.copyInto(newItems);
        this.setItems(newItems);
        this.setChanged();
    }

    /* ---------------- UI / name ---------------- */

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container." + HarambeCore.MOD_ID + ".anytomithium_chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInv) {
        return new AnyChestMenu(
                containerId,
                playerInv,
                this,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }

    /* ---------------- open / close & animation ---------------- */

    private void signalOpenCount(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
        // Matches vanilla chest behavior: sync to client via block event & update neighbors
        level.blockEvent(pos, state.getBlock(), 1, newCount);
        level.updateNeighborsAt(pos, state.getBlock());
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openers.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openers.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openers.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.recheckOpen();
    }

    /**
     * Called when the server sends block events; type 1 is our "open-count changed".
     * This is what actually drives the client-side lid animation.
     */
    @Override
    public boolean triggerEvent(int type, int data) {
        if (type == 1) {
            this.lid.shouldBeOpen(data > 0);
            return true;
        }
        return super.triggerEvent(type, data);
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return this.lid.getOpenness(partialTicks);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AnyChestBlockEntity be) {
        be.lid.tickLid(); // smooth open/close animation
    }

    /* ---------------- persistence (ValueInput / ValueOutput) ---------------- */

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, this.items);
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }
}
