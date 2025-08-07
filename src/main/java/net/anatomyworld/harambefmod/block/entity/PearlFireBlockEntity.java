package net.anatomyworld.harambefmod.block.entity;

import net.anatomyworld.harambefmod.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.jetbrains.annotations.NotNull;

/**
 * BlockEntity for PearlFireBlock, storing a unique color for the fire.
 */
public class PearlFireBlockEntity extends BlockEntity {
    private int color;  // RGB color value (0xRRGGBB) for this fire's tint

    public PearlFireBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PEARL_FIRE_BLOCK_ENTITY.get(), pos, state);
        this.color = 0xFFFFFF; // default color: white (no tint) until assigned
    }

    /** Get the color tint of this fire (as 0xRRGGBB). */
    public int getColor() {
        return color;
    }

    /** Set a new color tint for this fire. Should be called on the server side. */
    public void setColor(int color) {
        this.color = color;
        setChanged();  // mark data as changed for saving
        // Notify clients of the change so the color update is visible immediately
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("Color", this.color);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("Color")) {
            this.color = tag.getInt("Color");
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // Creates a packet with the BlockEntity's NBT data to sync with the client
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
        // Provide the current state for client synchronization
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // No automatic random color assignment here; color will be set explicitly when placed.
    }
}
