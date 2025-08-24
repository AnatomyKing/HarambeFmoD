package net.anatomyworld.harambefmod.block.entity;

import net.anatomyworld.harambefmod.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BlockEntity for PearlFireBlock, storing a unique color for the fire.
 */
public final class PearlFireBlockEntity extends BlockEntity {
    private int color = 0xFFFFFF; // RGB color value (0xRRGGBB) for this fire's tint

    public PearlFireBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PEARL_FIRE_BLOCK_ENTITY.get(), pos, state);
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
            this.level.sendBlockUpdated(this.worldPosition, state, state,
                    Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        }
    }

    // ---------- NBT (save/load) ----------

    @Override
    protected void saveAdditional(@NotNull ValueOutput out) {
        out.putInt("Color", this.color);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput in) {
        // Use the new Optional-friendly read helpers
        this.color = in.getIntOr("Color", this.color);
    }

    // ---------- Networking sync (chunk load + block updates) ----------

    /** Used on normal block updates to send a BE data packet to clients. */
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** Used when the chunk is (re)sent to the client. */
    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // No automatic random color assignment here; color will be set explicitly when placed.
    }
}
