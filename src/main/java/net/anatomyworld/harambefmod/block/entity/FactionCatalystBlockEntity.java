package net.anatomyworld.harambefmod.block.entity;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.client.render.FactionCatalystAreaOverlay;
import net.anatomyworld.harambefmod.faction.CatalystRegistry;
import net.anatomyworld.harambefmod.faction.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.event.EventHooks;

public abstract class FactionCatalystBlockEntity extends BlockEntity
        implements GameEventListener.Provider<SculkCatalystBlockEntity.CatalystListener> {

    private static final int CONVERT_RADIUS = 16;
    private static final float AUTO_BONEMEAL_CHANCE = 0.25f;

    private final SculkCatalystBlockEntity.CatalystListener listener;

    /** Wall-clock expiry; 0 = inactive. Authoritative value persisted to disk. */
    private long expiresAtMs = 0L;

    /** Whether the client overlay is visible (synced + persisted). Default: true. */
    private boolean overlayVisible = true;

    protected FactionCatalystBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,
                                         BlockPos pos, BlockState state) {
        super(type, pos, state);
        PositionSource src = new BlockPositionSource(pos);
        this.listener = new SculkCatalystBlockEntity.CatalystListener(state, src);
    }

    public abstract Faction faction();
    protected abstract Block grassBlock();
    protected abstract Block veinBlock();

    @Override
    public SculkCatalystBlockEntity.CatalystListener getListener() { return listener; }

    /* ---------- persistence (1.21.6+ Value I/O) ---------- */

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putLong("ExpiresAtMs", Math.max(0L, this.expiresAtMs));
        out.putBoolean("OverlayVisible", this.overlayVisible);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        this.expiresAtMs = Math.max(0L, in.getLongOr("ExpiresAtMs", 0L));
        this.overlayVisible = in.getBooleanOr("OverlayVisible", true);
    }

    public void setExpiresAtMs(long ms) {
        this.expiresAtMs = Math.max(0L, ms);
        setChanged(); // mark BE dirty so the chunk saves
    }

    public long getExpiresAtMs() { return Math.max(0L, expiresAtMs); }

    public boolean isOverlayVisible() { return overlayVisible; }
    public void setOverlayVisible(boolean visible) { this.overlayVisible = visible; }

    /* ---------- lifecycle ---------- */

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level == null) return;

        if (this.level.isClientSide) {
            // Respect saved visibility: track/untrack accordingly
            if (this.overlayVisible) {
                FactionCatalystAreaOverlay.track(this.level.dimension(), this.worldPosition);
            } else {
                FactionCatalystAreaOverlay.untrack(this.level.dimension(), this.worldPosition);
            }
        } else {
            CatalystRegistry.put(this.level.dimension(), this.worldPosition, this.faction(), this.expiresAtMs);
            HarambeCore.LOGGER.info("[CATALYST] placed faction={} pos={} expiresAtMs={}",
                    this.faction(), this.worldPosition, this.expiresAtMs);
            setChanged();
        }
    }

    @Override
    public void setRemoved() {
        if (this.level != null) {
            if (this.level.isClientSide) {
                FactionCatalystAreaOverlay.untrack(this.level.dimension(), this.worldPosition);
            } else {
                CatalystRegistry.remove(this.level.dimension(), this.worldPosition);
                HarambeCore.LOGGER.info("[CATALYST] removed faction={} pos={}", this.faction(), this.worldPosition);
            }
        }
        super.setRemoved();
    }

    /* ---------- tick: convert sculk to faction materials ---------- */

    public static void serverTick(Level level, BlockPos pos, BlockState state, FactionCatalystBlockEntity be) {
        if (level.isClientSide) return;
        ServerLevel srv = (ServerLevel) level;

        LongOpenHashSet before = snapshotSculkAndVeins(srv, pos, CONVERT_RADIUS);
        be.listener.getSculkSpreader().updateCursors(level, pos, level.getRandom(), true);
        LongOpenHashSet after = snapshotSculkAndVeins(srv, pos, CONVERT_RADIUS);
        after.removeAll(before);

        if (after.isEmpty()) return;

        RandomSource rnd = srv.getRandom();

        for (long packed : after.toLongArray()) {
            BlockPos p = BlockPos.of(packed);
            BlockState s = srv.getBlockState(p);

            if (s.is(Blocks.SCULK)) {
                srv.setBlock(p, be.grassBlock().defaultBlockState(), 3);
                if (rnd.nextFloat() < AUTO_BONEMEAL_CHANCE) {
                    fireBonemealForFactionGrass(srv, p, be.grassBlock());
                }
                continue;
            }

            if (s.is(Blocks.SCULK_VEIN)) {
                if (hasFactionGrassNeighbor(srv, p, be.grassBlock())) {
                    srv.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    continue;
                }

                BlockState out = be.veinBlock().defaultBlockState();
                for (Direction d : Direction.values()) {
                    var face = MultifaceBlock.getFaceProperty(d);
                    if (face != null && s.hasProperty(face) && s.getValue(face)) {
                        out = out.setValue(face, true);
                    }
                }
                if (s.hasProperty(BlockStateProperties.WATERLOGGED) && out.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    out = out.setValue(BlockStateProperties.WATERLOGGED, s.getValue(BlockStateProperties.WATERLOGGED));
                }

                if (hasFactionGrassNeighbor(srv, p, be.grassBlock())) {
                    srv.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                } else {
                    srv.setBlock(p, out, 3);
                }
            }
        }
        stripVeinFacesFromCatalyst(srv, pos, be.veinBlock());
    }

    /* ---------- helpers ---------- */

    private static LongOpenHashSet snapshotSculkAndVeins(ServerLevel srv, BlockPos center, int r) {
        LongOpenHashSet set = new LongOpenHashSet();
        BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    cur.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState s = srv.getBlockState(cur);
                    if (s.is(Blocks.SCULK) || s.is(Blocks.SCULK_VEIN)) {
                        set.add(cur.asLong());
                    }
                }
            }
        }
        return set;
    }

    private static boolean hasFactionGrassNeighbor(Level lvl, BlockPos pos, Block grassBlock) {
        for (Direction d : Direction.values()) {
            if (lvl.getBlockState(pos.relative(d)).is(grassBlock)) return true;
        }
        return false;
    }

    private static void fireBonemealForFactionGrass(ServerLevel level, BlockPos pos, Block grassBlock) {
        var now = level.getBlockState(pos);
        if (!now.is(grassBlock)) return;
        EventHooks.fireBonemealEvent(null, level, pos, now, new ItemStack(Items.BONE_MEAL));
    }

    private static void stripVeinFacesFromCatalyst(ServerLevel level, BlockPos catalystPos, Block factionVein) {
        for (Direction d : Direction.values()) {
            BlockPos neighbor = catalystPos.relative(d);
            BlockState s = level.getBlockState(neighbor);
            if (!s.is(factionVein)) continue;

            var faceProp = MultifaceBlock.getFaceProperty(d.getOpposite());
            if (faceProp != null && s.hasProperty(faceProp) && s.getValue(faceProp)) {
                BlockState newState = s.setValue(faceProp, false);

                boolean anyFaceLeft = false;
                for (Direction d2 : Direction.values()) {
                    var f2 = MultifaceBlock.getFaceProperty(d2);
                    if (f2 != null && newState.hasProperty(f2) && newState.getValue(f2)) { anyFaceLeft = true; break; }
                }
                level.setBlock(neighbor, anyFaceLeft ? newState : Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }
}
