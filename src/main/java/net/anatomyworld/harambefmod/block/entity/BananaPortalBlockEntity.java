package net.anatomyworld.harambefmod.block.entity;

import net.anatomyworld.harambefmod.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.NotNull;

/** Stores tint color, anchor, and this portal group's front direction. */
public final class BananaPortalBlockEntity extends BlockEntity {
    private int color = 0xFFFFFF; // 0xRRGGBB
    private BlockPos anchor = BlockPos.ZERO; // interior bottom-left
    private Direction front = Direction.SOUTH; // portal "front"

    public BananaPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BANANA_PORTAL_BE.get(), pos, state);
    }

    public int getColor() { return color; }

    /** Call on the SERVER; this will sync to clients. */
    public void setColor(int rgb) {
        this.color = rgb;
        setChanged();
        if (level != null && !level.isClientSide) {
            var s = getBlockState();
            // Triggers BE update packet to all tracking clients
            level.sendBlockUpdated(worldPosition, s, s, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        }
    }

    public BlockPos getAnchor() { return anchor; }
    public void setAnchor(BlockPos p) { this.anchor = p; setChanged(); }

    public Direction getFront() { return front; }
    public void setFront(Direction d) {
        if (d.getAxis() == Direction.Axis.Y) return;
        this.front = d; setChanged();
        if (level != null && !level.isClientSide) {
            var s = getBlockState();
            level.sendBlockUpdated(worldPosition, s, s, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        }
    }

    // ---------- NBT (save/load) ----------

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("Color", color);
        tag.putInt("AX", anchor.getX());
        tag.putInt("AY", anchor.getY());
        tag.putInt("AZ", anchor.getZ());
        tag.putString("Front", front.getName());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("Color")) color = tag.getInt("Color");
        anchor = new BlockPos(tag.getInt("AX"), tag.getInt("AY"), tag.getInt("AZ"));
        Direction f = Direction.byName(tag.getString("Front"));
        if (f != null && f.getAxis() != Direction.Axis.Y) front = f;
    }

    // ---------- Networking sync (chunk load + block updates) ----------

    /** Used when the chunk is (re)sent to the client. */
    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
        // Small and safe: just reuse our existing saver
        return this.saveWithoutMetadata(provider);
    }

    /** Used on normal block updates to send a BE data packet to clients. */
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // Uses the tag from getUpdateTag
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
