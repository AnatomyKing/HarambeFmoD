package net.anatomyworld.harambefmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.anatomyworld.harambefmod.block.entity.BananaPortalBlockEntity;
import net.anatomyworld.harambefmod.world.BananaPortalShape;
import net.anatomyworld.harambefmod.world.PortalLinkData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Thin, panel-like portal block with re-entry teleport and velocity/orientation carry-over. */
public final class BananaPortalBlock extends Block implements EntityBlock {
    public static final MapCodec<BananaPortalBlock> CODEC = BlockBehaviour.simpleCodec(p -> new BananaPortalBlock());
    @Override public @NotNull MapCodec<? extends Block> codec() { return CODEC; }

    /** X = portal plane ⟂ X (normal ±Z). Z = portal plane ⟂ Z (normal ±X). */
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    private static final VoxelShape SHAPE_X = Block.box(0, 0, 7, 16, 16, 9); // normal along Z
    private static final VoxelShape SHAPE_Z = Block.box(7, 0, 0, 9, 16, 16); // normal along X

    // Per-entity tags
    private static final String TAG_CD_UNTIL   = "harambefmod:portal_cd";     // long tick until re-entry allowed
    private static final String TAG_ANY_TICK   = "harambefmod:any_portal_t";  // last tick seen inside ANY portal
    private static final String TAG_IN_ANCHOR  = "harambefmod:in_anchor";     // last anchor seen (debug/optional)

    private static final long   COOLDOWN_TICKS = 10L;          // short, Nether-like feel
    private static final double EJECT          = 0.03125 * 4;  // 2 px outward from the plane

    public BananaPortalBlock() {
        super(BlockBehaviour.Properties.of()
                .noOcclusion()
                .noCollission()
                .strength(-1.0F, 3_600_000.0F)
                .lightLevel(s -> 11)
                .sound(SoundType.GLASS)
                .noLootTable()
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(AXIS); }
    @Override public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) { return new BananaPortalBlockEntity(pos, state); }
    @Override public @NotNull VoxelShape getShape(@NotNull BlockState s, @NotNull BlockGetter g, @NotNull BlockPos p, @NotNull CollisionContext c) { return s.getValue(AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z; }

    @Override
    public void entityInside(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
        if (level.isClientSide) return;
        ServerLevel server = (ServerLevel) level;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BananaPortalBlockEntity portalBE)) return;

        long now        = server.getGameTime();
        long thisAnchor = portalBE.getAnchor().asLong();

        var tag         = entity.getPersistentData();
        long lastAny    = tag.getLong(TAG_ANY_TICK);

        // --- Global re-entry gate: only if we were OUT of ANY portal for at least one full tick ---
        boolean enteredFromOutside = (lastAny < (now - 1));   // prevents same-tick + linger retriggers
        tag.putLong(TAG_ANY_TICK, now);                       // mark "we are inside a portal this tick"
        tag.putLong(TAG_IN_ANCHOR, thisAnchor);               // (optional) remember which interior we touched

        if (!enteredFromOutside) return;                      // lingering inside -> do nothing

        // Cooldown must also have elapsed
        if (tag.getLong(TAG_CD_UNTIL) > now) return;

        // Find the linked endpoint
        var data = PortalLinkData.get(server);
        var targetOpt = data.findOtherEndpointForPosition(server, pos);
        if (targetOpt.isEmpty()) return;
        var target = targetOpt.get();
        if (server.dimension() != target.dim) return; // same-dimension only per your spec

        // --- Bases (source & destination) ---
        Vec3 nSrc    = dirToUnit(portalBE.getFront());
        Vec3 nDstOut = dirToUnit(target.front.getOpposite()); // always exit away from dest front
        Vec3 up      = new Vec3(0, 1, 0);

        Vec3 rSrc = up.cross(nSrc).normalize();
        if (rSrc.lengthSqr() < 1e-8) rSrc = new Vec3(1, 0, 0);
        Vec3 rDst = up.cross(nDstOut).normalize();
        if (rDst.lengthSqr() < 1e-8) rDst = new Vec3(1, 0, 0);

        // --- Rotate velocity straight-through ---
        Vec3 v    = entity.getDeltaMovement();
        double vr = v.dot(rSrc), vu = v.dot(up), vf = v.dot(nSrc);
        double vfOut = Math.abs(vf);
        Vec3 vOut = rDst.scale(vr).add(up.scale(vu)).add(nDstOut.scale(vfOut));
        entity.setDeltaMovement(vOut);

        // --- Preserve lateral/vertical offset across panes + small eject outward ---
        var srcAnchor = portalBE.getAnchor();
        var tgtAnchor = target.anchor;
        Vec3 srcCenter = new Vec3(srcAnchor.getX() + 0.5, srcAnchor.getY(), srcAnchor.getZ() + 0.5);
        double lateral = entity.position().subtract(srcCenter).dot(rSrc);
        double vertical = entity.getY() - srcAnchor.getY();
        Vec3 tgtCenter = new Vec3(tgtAnchor.getX() + 0.5, tgtAnchor.getY(), tgtAnchor.getZ() + 0.5);
        Vec3 outPos = tgtCenter.add(rDst.scale(lateral)).add(0, vertical, 0).add(nDstOut.scale(EJECT));

        // --- Outgoing yaw: follow motion, else face away from dest front ---
        float yawOut;
        Vec3 hv = new Vec3(vOut.x, 0, vOut.z);
        if (hv.lengthSqr() > 1.0e-5) yawOut = (float) Math.toDegrees(Math.atan2(-vOut.x, vOut.z));
        else                          yawOut = target.front.getOpposite().toYRot();

        if (entity instanceof ServerPlayer sp) {
            sp.teleportTo(server, outPos.x, outPos.y, outPos.z, yawOut, sp.getXRot());
        } else {
            entity.moveTo(outPos.x, outPos.y, outPos.z, yawOut, entity.getXRot());
        }

        // Arm cooldown; keep TAG_ANY_TICK at 'now' so lingering never retriggers until you leave.
        tag.putLong(TAG_CD_UNTIL, now + COOLDOWN_TICKS);
        tag.putLong(TAG_IN_ANCHOR, tgtAnchor.asLong()); // optional: mark which pane we landed in
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                @NotNull Block neighbor, @NotNull BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;
        ServerLevel server = (ServerLevel) level;
        if (!BananaPortalShape.isInteriorStillFramed(server, pos)) {
            PortalLinkData.get(server).removePortalAt(server, pos, true);
        }
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide && player.isCreative()) {
            PortalLinkData.get((ServerLevel) level).removePortalAt((ServerLevel) level, pos, true);
        }
        super.playerWillDestroy(level, pos, state, player);
        return state;
    }

    private static Vec3 dirToUnit(Direction d) {
        return switch (d) {
            case NORTH -> new Vec3(0, 0, -1);
            case SOUTH -> new Vec3(0, 0,  1);
            case WEST  -> new Vec3(-1, 0, 0);
            case EAST  -> new Vec3( 1, 0, 0);
            default    -> new Vec3(0, 0, 1);
        };
    }
}
