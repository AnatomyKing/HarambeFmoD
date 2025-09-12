// AnyChestBlockEntity.java
package net.anatomyworld.harambefmod.block.entity;

import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class AnyChestBlockEntity extends BaseContainerBlockEntity implements LidBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);

    private final ContainerOpenersCounter openers = new ContainerOpenersCounter() {
        @Override protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, 0.9F + level.random.nextFloat()*0.1F);
        }
        @Override protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, 0.9F + level.random.nextFloat()*0.1F);
        }
        @Override protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
            lid.shouldBeOpen(newCount > 0); // ✅ drive the animation
        }
        @Override protected boolean isOwnContainer(Player player) {
            // Good enough for single chest (no double container pairings)
            return player.containerMenu instanceof ChestMenu menu && menu.getContainer() == AnyChestBlockEntity.this;
        }
    };

    private final ChestLidController lid = new ChestLidController();

    public AnyChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANY_CHEST_ENTITY.get(), pos, state); // ✅ correct type
    }

    /* -------- inventory -------- */
    @Override public int getContainerSize() { return items.size(); }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }

    /* -------- UI/Name -------- */
    @Override protected Component getDefaultName() {
        return Component.translatable("container." + HarambeCore.MOD_ID + ".anytomithium_chest");
    }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return ChestMenu.threeRows(id, inv, this);
    }

    /* -------- open/close hooks -------- */
    @Override public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) openers.incrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
    }
    @Override public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) openers.decrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
    }
    @Override public float getOpenNess(float partialTicks) {
        return lid.getOpenness(partialTicks);
    }
    public static void clientTick(Level level, BlockPos pos, BlockState state, AnyChestBlockEntity be) {
        be.lid.tickLid(); // smooth animation
    }

    /* -------- persistence (1.21.6+ ValueInput/Output) -------- */
    @Override
    protected void loadAdditional(ValueInput in) { // ✅ new signature
        super.loadAdditional(in);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, items);   // ✅ new API
    }
    @Override
    protected void saveAdditional(ValueOutput out) { // ✅ new signature
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, items);  // ✅ new API
    }

    @Override public void clearContent() { items.clear(); setChanged(); }
}
