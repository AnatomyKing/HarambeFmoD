package net.anatomyworld.harambefmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.anatomyworld.harambefmod.block.entity.BananaPortalBlockEntity;
import net.anatomyworld.harambefmod.world.BananaPortalShape;
import net.anatomyworld.harambefmod.world.PortalLinkData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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

/** Re-entry gated portal with look/velocity basis transforms and momentum-friendly teleport. */
public final class BananaPortalBlock extends Block implements EntityBlock {
    public static final MapCodec<BananaPortalBlock> CODEC = BlockBehaviour.simpleCodec(p -> new BananaPortalBlock());
    @Override public @NotNull MapCodec<? extends Block> codec() { return CODEC; }

    /** X = portal plane ⟂ X (normal ±Z). Z = portal plane ⟂ Z (normal ±X). */
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    private static final VoxelShape SHAPE_X = Block.box(0, 0, 7, 16, 16, 9); // normal along Z
    private static final VoxelShape SHAPE_Z = Block.box(7, 0, 0, 9, 16, 16); // normal along X

    // Per-entity tags
    private static final String TAG_CD_UNTIL  = "harambefmod:portal_cd";    // long tick until re-entry allowed
    private static final String TAG_ANY_TICK  = "harambefmod:any_portal_t"; // last tick seen inside ANY portal
    private static final String TAG_IN_ANCHOR = "harambefmod:in_anchor";    // last anchor (optional)

    private static final long   COOLDOWN_TICKS = 10L;          // short, Nether-like feel
    private static final double EJECT          = 0.03125 * 4;  // 2 px outward

    // Tunables for “flow-through” feel
    private static final double MIN_OUT_WALK   = 0.04;  // tiny nudge when walking
    private static final double MIN_OUT_SPRINT = 0.12;  // gentle nudge when sprinting

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

        var tag      = entity.getPersistentData();
        long lastAny = tag.getLong(TAG_ANY_TICK);

        // Global re-entry gate: must have been OUT of ALL portals for >= 1 full tick
        boolean enteredFromOutside = (lastAny < (now - 1));
        tag.putLong(TAG_ANY_TICK, now);
        tag.putLong(TAG_IN_ANCHOR, thisAnchor);

        if (!enteredFromOutside) return;
        if (tag.getLong(TAG_CD_UNTIL) > now) return;

        // Find linked endpoint
        var data = PortalLinkData.get(server);
        var targetOpt = data.findOtherEndpointForPosition(server, pos);
        if (targetOpt.isEmpty()) return;
        var target = targetOpt.get();
        if (server.dimension() != target.dim) return; // same-dimension per your spec

        // Build orthonormal bases (source & destination)
        Vec3 nSrc    = dirToUnit(portalBE.getFront());
        Vec3 nDstOut = dirToUnit(target.front.getOpposite());
        Vec3 up      = new Vec3(0, 1, 0);

        Vec3 rSrc = up.cross(nSrc).normalize();
        if (rSrc.lengthSqr() < 1e-8) rSrc = new Vec3(1, 0, 0);
        Vec3 rDst = up.cross(nDstOut).normalize();
        if (rDst.lengthSqr() < 1e-8) rDst = new Vec3(1, 0, 0);

        // --- Momentum handling ---
        Vec3 vIn  = entity.getDeltaMovement();
        double vr = vIn.dot(rSrc), vu = vIn.dot(up), vf = vIn.dot(nSrc);
        double vfOut = Math.abs(vf);
        Vec3 vOut = rDst.scale(vr).add(up.scale(vu)).add(nDstOut.scale(vfOut));

        // tiny outward nudge so shallow entries don't feel sticky
        boolean sprinting = (entity instanceof LivingEntity le) && le.isSprinting();
        double minOut = sprinting ? MIN_OUT_SPRINT : MIN_OUT_WALK;
        double outComp = vOut.dot(nDstOut);
        if (outComp < minOut && vIn.lengthSqr() > 0.01) {
            vOut = vOut.add(nDstOut.scale(minOut - outComp));
        }

        // --- Preserve lateral/vertical offset + small eject outward
        var srcAnchor = portalBE.getAnchor();
        var tgtAnchor = target.anchor;
        Vec3 srcCenter = new Vec3(srcAnchor.getX() + 0.5, srcAnchor.getY(), srcAnchor.getZ() + 0.5);
        double lateral  = entity.position().subtract(srcCenter).dot(rSrc);
        double vertical = entity.getY() - srcAnchor.getY();
        Vec3 tgtCenter  = new Vec3(tgtAnchor.getX() + 0.5, tgtAnchor.getY(), tgtAnchor.getZ() + 0.5);
        Vec3 outPos     = tgtCenter.add(rDst.scale(lateral)).add(0, vertical, 0).add(nDstOut.scale(EJECT));

        // --- Rotate LOOK vector → perfect yaw & pitch (Minecraft pitch: up is negative)
        Vec3 look      = entity.getLookAngle().normalize();
        double lr = look.dot(rSrc), lu = look.dot(up), lf = look.dot(nSrc);
        Vec3 lookOut   = rDst.scale(lr).add(up.scale(lu)).add(nDstOut.scale(lf)).normalize();

        float yawOut   = (float) Math.toDegrees(Math.atan2(-lookOut.x, lookOut.z));
        double horiz   = Math.sqrt(lookOut.x * lookOut.x + lookOut.z * lookOut.z);
        float pitchOut = (float) Math.toDegrees(Math.atan2(-lookOut.y, horiz));
        pitchOut = Mth.clamp(pitchOut, -90.0f, 90.0f);

        if (!Float.isFinite(yawOut))   yawOut   = target.front.getOpposite().toYRot();
        if (!Float.isFinite(pitchOut)) pitchOut = entity.getXRot();

        // Snapshot motion flags to preserve feel across teleport
        boolean wasSprinting = (entity instanceof LivingEntity le2) && le2.isSprinting();
        boolean wasElytra    = (entity instanceof LivingEntity le3) && le3.isFallFlying(); // on LivingEntity

        // --- Teleport, then re-apply velocity and flags ---
        if (entity instanceof ServerPlayer sp) {
            sp.setDeltaMovement(vOut);
            sp.teleportTo(server, outPos.x, outPos.y, outPos.z, yawOut, pitchOut);
            sp.setDeltaMovement(vOut);           // reapply after teleport (important)
            sp.setYBodyRot(yawOut);
            sp.setYHeadRot(yawOut);
            sp.setXRot(pitchOut);
            sp.setSprinting(wasSprinting);
            if (wasElytra) sp.startFallFlying(); // ✅ only players have startFallFlying()
        } else {
            entity.setDeltaMovement(vOut);
            entity.moveTo(outPos.x, outPos.y, outPos.z, yawOut, pitchOut);
            entity.setDeltaMovement(vOut);
            if (entity instanceof LivingEntity le) {
                le.setYBodyRot(yawOut);
                le.setYHeadRot(yawOut);
                le.setSprinting(wasSprinting);
                // ❌ no startFallFlying() here — non-players don't have it in 1.21.x
            }
        }

        // Cooldown; lingering inside won't retrigger until you actually leave
        tag.putLong(TAG_CD_UNTIL, now + COOLDOWN_TICKS);
        tag.putLong(TAG_IN_ANCHOR, tgtAnchor.asLong());
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
