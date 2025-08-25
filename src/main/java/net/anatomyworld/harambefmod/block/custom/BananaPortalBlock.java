package net.anatomyworld.harambefmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.entity.BananaPortalBlockEntity;
import net.anatomyworld.harambefmod.world.BananaPortalShape;
import net.anatomyworld.harambefmod.world.PortalLinkData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Re-entry gated portal with ultra-smooth, survival-safe teleport. */
public final class BananaPortalBlock extends Block implements EntityBlock {
    public static final MapCodec<BananaPortalBlock> CODEC = BlockBehaviour.simpleCodec(BananaPortalBlock::new);
    @Override protected @NotNull MapCodec<? extends Block> codec() { return CODEC; }

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    private static final VoxelShape SHAPE_X = Block.box(0, 0, 7, 16, 16, 9);
    private static final VoxelShape SHAPE_Z = Block.box(7, 0, 0, 9, 16, 16);

    // Per-entity tags (persist across ticks)
    private static final String TAG_CD_UNTIL  = "harambefmod:portal_cd";    // long tick until re-entry allowed
    private static final String TAG_ANY_TICK  = "harambefmod:any_portal_t"; // last tick seen inside ANY portal
    private static final String TAG_IN_ANCHOR = "harambefmod:in_anchor";    // last anchor (optional)

    private static final long   COOLDOWN_TICKS = 10L;          // short, nether-like feel
    private static final double EJECT          = 0.125D;       // 2 px outward

    // Flow-through feel
    private static final double MIN_OUT_WALK   = 0.04;  // tiny nudge when walking
    private static final double MIN_OUT_SPRINT = 0.12;  // gentle nudge when sprinting

    // Safety pads
    private static final double WALL_PAD  = 0.125; // 2/16 block lateral clearance to frame faces
    private static final double CEIL_PAD  = 0.125; // 2/16 block headroom under the top span
    private static final double NEAR_EPS  = 0.02;

    // Post-teleport easing
    private static final int SMOOTH_STEPS = 3;
    private static final double[] SMOOTH_FACTORS = { 0.60, 0.85, 1.00 };

    public BananaPortalBlock(BlockBehaviour.Properties props) {
        super(props.noOcclusion()
                .noCollission()
                .strength(-1.0F, 3_600_000.0F)
                .lightLevel(s -> 11)
                .sound(SoundType.GLASS)
                .noLootTable());
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(AXIS); }

    @Override public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BananaPortalBlockEntity(pos, state);
    }

    @Override public @NotNull VoxelShape getShape(@NotNull BlockState s, @NotNull BlockGetter g, @NotNull BlockPos p, @NotNull CollisionContext c) {
        return s.getValue(AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    // 1.21.x: signature now includes InsideBlockEffectApplier (5 params), override is protected
    @Override
    protected void entityInside(@NotNull BlockState state,
                                @NotNull Level level,
                                @NotNull BlockPos pos,
                                @NotNull Entity entity,
                                @NotNull InsideBlockEffectApplier effectApplier) {
        if (level.isClientSide) return;
        ServerLevel server = (ServerLevel) level;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BananaPortalBlockEntity portalBE)) return;

        long now        = server.getGameTime();
        long thisAnchor = portalBE.getAnchor().asLong();

        // NBT in 1.21.8: use ...Or helpers instead of Optional<Long>
        CompoundTag tag = entity.getPersistentData();
        long lastAny = tag.getLongOr(TAG_ANY_TICK, 0L);

        // Re-entry gate: must have been OUT of ALL portals for >= 1 full tick
        boolean enteredFromOutside = (lastAny < (now - 1));
        tag.putLong(TAG_ANY_TICK, now);
        tag.putLong(TAG_IN_ANCHOR, thisAnchor);
        if (!enteredFromOutside) return;
        if (tag.getLongOr(TAG_CD_UNTIL, 0L) > now) return;

        // Linked endpoint
        var data = PortalLinkData.get(server);
        var targetOpt = data.findOtherEndpointForPosition(server, pos);
        if (targetOpt.isEmpty()) return;
        var target = targetOpt.get();
        if (!server.dimension().equals(target.dim)) return; // same-dimension only

        // Bases
        Vec3 nSrc    = dirToUnit(portalBE.getFront());
        Vec3 nDstOut = dirToUnit(target.front.getOpposite());
        Vec3 up      = new Vec3(0, 1, 0);
        Vec3 rSrc = up.cross(nSrc).normalize();
        if (rSrc.lengthSqr() < 1e-8) rSrc = new Vec3(1, 0, 0);
        Vec3 rDst = up.cross(nDstOut).normalize();
        if (rDst.lengthSqr() < 1e-8) rDst = new Vec3(1, 0, 0);

        // Momentum (compute vOut; apply after handshake)
        Vec3 vIn  = entity.getDeltaMovement();
        double vr = vIn.dot(rSrc), vu = vIn.dot(up), vf = vIn.dot(nSrc);
        double vfOut = Math.abs(vf);
        Vec3 vOut = rDst.scale(vr).add(up.scale(vu)).add(nDstOut.scale(vfOut));
        boolean sprinting = (entity instanceof LivingEntity le) && le.isSprinting();
        double minOut = sprinting ? MIN_OUT_SPRINT : MIN_OUT_WALK;
        double outComp = vOut.dot(nDstOut);
        if (outComp < minOut && vIn.lengthSqr() > 0.01) vOut = vOut.add(nDstOut.scale(minOut - outComp));

        // === Size-agnostic placement + safety margins ===
        Direction.Axis axis = state.getValue(AXIS);
        BlockPos srcAnchor  = portalBE.getAnchor();
        BlockPos tgtAnchor  = target.anchor;

        Vec3 srcBase = new Vec3(srcAnchor.getX() + 0.5, srcAnchor.getY(), srcAnchor.getZ() + 0.5);
        double Ls = entity.position().subtract(srcBase).dot(rSrc); // lateral (blocks)
        double Vs = entity.getY() - srcAnchor.getY();              // vertical from bottom (blocks)

        // Source/destination sizes (scan current interior)
        int srcW = measureWidth(server, srcAnchor, axis);
        int srcH = measureHeight(server, srcAnchor, axis);
        int dstW = target.width;
        int dstH = target.height;

        // Normalize to [0..1] in source
        double u = (srcW > 1) ? Mth.clamp(Ls / (srcW - 1.0), 0.0, 1.0) : 0.5;
        double srcAvailV = Math.max(0.001, srcH - entity.getBbHeight());
        double v = Mth.clamp(Vs / srcAvailV, 0.0, 1.0);

        // Rescale to destination ideal offsets
        double LdIdeal   = u * (dstW - 1.0);
        double dstAvailV = Math.max(0.0, dstH - entity.getBbHeight());
        double VdIdeal   = v * dstAvailV;

        // Safe lateral window, based on hitbox width
        double halfW = entity.getBbWidth() / 2.0;
        double minLd = (halfW + WALL_PAD) - 0.5;
        double maxLd = (dstW - 1.0) - ((halfW + WALL_PAD) - 0.5);
        boolean invalidLane = minLd > maxLd;
        double Ld;
        boolean nearLeft = false, nearRight = false;

        if (invalidLane) {
            Ld = (dstW - 1.0) * 0.5;
        } else {
            Ld = Mth.clamp(LdIdeal, minLd, maxLd);
            nearLeft  = (LdIdeal <= minLd + NEAR_EPS);
            nearRight = (LdIdeal >= maxLd - NEAR_EPS);
        }

        // Safe vertical (headroom under top span)
        double maxVd = Math.max(0.0, dstH - entity.getBbHeight() - CEIL_PAD);
        double Vd = Mth.clamp(VdIdeal, 0.0, maxVd);
        boolean nearTop = (VdIdeal >= maxVd - NEAR_EPS);

        // World-space destination (bottom anchored)
        Vec3 tgtBase = new Vec3(tgtAnchor.getX() + 0.5, tgtAnchor.getY(), tgtAnchor.getZ() + 0.5);
        Vec3 outPos  = tgtBase.add(rDst.scale(Ld)).add(0, Vd, 0).add(nDstOut.scale(EJECT));

        // Edge-aware first moment: kill components that would push into walls/ceiling
        double vLat = vOut.dot(rDst);
        if (nearLeft && vLat < 0)  vOut = vOut.subtract(rDst.scale(vLat));
        if (nearRight && vLat > 0) vOut = vOut.subtract(rDst.scale(vLat));
        if (nearTop && vOut.y > 0) vOut = new Vec3(vOut.x, 0.0, vOut.z);

        // Look transform (yaw/pitch)
        Vec3 look    = entity.getLookAngle().normalize();
        double lr = look.dot(rSrc), lu = look.dot(up), lf = look.dot(nSrc);
        Vec3 lookOut = rDst.scale(lr).add(up.scale(lu)).add(nDstOut.scale(lf)).normalize();
        float yawOut   = (float) Math.toDegrees(Math.atan2(-lookOut.x, lookOut.z));
        double horiz   = Math.sqrt(lookOut.x * lookOut.x + lookOut.z * lookOut.z);
        float pitchOut = (float) Math.toDegrees(Math.atan2(-lookOut.y, horiz));
        pitchOut = Mth.clamp(pitchOut, -90.0f, 90.0f);
        if (!Float.isFinite(yawOut))   yawOut   = target.front.getOpposite().toYRot();
        if (!Float.isFinite(pitchOut)) pitchOut = entity.getXRot();

        boolean wasSprinting = (entity instanceof LivingEntity le2) && le2.isSprinting();
        boolean wasElytra    = (entity instanceof LivingEntity le3) && le3.isFallFlying();

        // Ensure the initial exit position is collision-free
        outPos = findSafeExit(server, entity, outPos, nDstOut, tgtAnchor, rDst, dstW, dstH);

        // Gate re-entry briefly
        tag.putLong(TAG_CD_UNTIL, now + COOLDOWN_TICKS);
        tag.putLong(TAG_IN_ANCHOR, tgtAnchor.asLong());
        entity.resetFallDistance();

        final Vec3  vOutTarget   = vOut;
        final float yawTarget    = yawOut;
        final float pitchTarget  = pitchOut;
        final boolean sprintFlag = wasSprinting;
        final boolean elytraFlag = wasElytra;

        // === Ultra-smooth handoff: teleport now, then ease vel+rot for a couple ticks ===
        Vec3 finalOutPos = outPos;
        runNextTick(server, () -> {
            if (entity.isRemoved()) return;
            if (entity instanceof ServerPlayer sp) {
                sp.connection.teleport(finalOutPos.x, finalOutPos.y, finalOutPos.z, yawTarget, pitchTarget);
                sp.resetFallDistance();
                sp.setSprinting(sprintFlag);
                if (elytraFlag) sp.startFallFlying();
                sp.setYBodyRot(yawTarget);
                sp.setYHeadRot(yawTarget);
                sp.setXRot(pitchTarget);
            } else {
                entity.teleportTo(finalOutPos.x, finalOutPos.y, finalOutPos.z);
                entity.setYRot(yawTarget);
                entity.setXRot(pitchTarget);
                entity.resetFallDistance();
                if (entity instanceof LivingEntity le) {
                    le.setSprinting(sprintFlag);
                    le.setYBodyRot(yawTarget);
                    le.setYHeadRot(yawTarget);
                }
            }
        });

        for (int i = 0; i < SMOOTH_STEPS; i++) {
            final int step = i;
            Vec3 finalRDst = rDst;
            runTicksLater(server, step + 1, () -> {
                if (entity.isRemoved()) return;

                double f = SMOOTH_FACTORS[Math.min(step, SMOOTH_FACTORS.length - 1)];

                // Lerp current velocity -> target
                Vec3 curV = entity.getDeltaMovement();
                Vec3 easedV = curV.scale(1.0 - f).add(vOutTarget.scale(f));
                entity.setDeltaMovement(easedV);

                // Ease yaw/pitch
                if (entity instanceof LivingEntity lv) {
                    float curYaw   = lv.getYHeadRot();
                    float curPitch = lv.getXRot();
                    float easedYaw   = lerpYawDegrees(curYaw, yawTarget, (float) f);
                    float easedPitch = (float) Mth.lerp(f, curPitch, pitchTarget);

                    if (entity instanceof ServerPlayer sp) {
                        sp.connection.teleport(sp.getX(), sp.getY(), sp.getZ(), easedYaw, easedPitch);
                        sp.setYBodyRot(easedYaw);
                        sp.setYHeadRot(easedYaw);
                        sp.setXRot(easedPitch);
                    } else {
                        entity.setYRot(easedYaw);
                        entity.setXRot(easedPitch);
                        lv.setYBodyRot(easedYaw);
                        lv.setYHeadRot(easedYaw);
                    }
                }

                // Keep inside safe lane
                clampInsideDestination(server, entity, finalRDst, up, tgtAnchor, target.axis, dstW, dstH);
                entity.resetFallDistance();
            });
        }

        runTicksLater(server, SMOOTH_STEPS + 1, () -> {
            if (entity.isRemoved()) return;
            entity.setDeltaMovement(vOutTarget);
            entity.resetFallDistance();
        });
    }

    // 1.21.x: Orientation-based signature
    @Override
    protected void neighborChanged(@NotNull BlockState state,
                                   @NotNull Level level,
                                   @NotNull BlockPos pos,
                                   @NotNull Block neighbor,
                                   @NotNull Orientation orientation,
                                   boolean isMoving) {
        if (level.isClientSide) return;
        ServerLevel server = (ServerLevel) level;
        if (!BananaPortalShape.isInteriorStillFramed(server, pos)) {
            PortalLinkData.get(server).removePortalAt(server, pos, true);
        }
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level,
                                                 @NotNull BlockPos pos,
                                                 @NotNull BlockState state,
                                                 @NotNull Player player) {
        if (!level.isClientSide && player.isCreative()) {
            PortalLinkData.get((ServerLevel) level).removePortalAt((ServerLevel) level, pos, true);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    // --- helpers ---

    private static Vec3 dirToUnit(Direction d) {
        return switch (d) {
            case NORTH -> new Vec3(0, 0, -1);
            case SOUTH -> new Vec3(0, 0,  1);
            case WEST  -> new Vec3(-1, 0, 0);
            case EAST  -> new Vec3( 1, 0, 0);
            default    -> new Vec3(0, 0, 1);
        };
    }

    private static int measureWidth(ServerLevel level, BlockPos anchor, Direction.Axis axis) {
        Direction right = (axis == Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;
        int w = 0;
        while (w < 64 && level.getBlockState(anchor.relative(right, w)).is(ModBlocks.BANANA_PORTAL.get())) w++;
        return w;
    }

    private static int measureHeight(ServerLevel level, BlockPos anchor, Direction.Axis axis) {
        int h = 0;
        while (h < 64 && level.getBlockState(anchor.above(h)).is(ModBlocks.BANANA_PORTAL.get())) h++;
        return h;
    }

    private static float wrapDegrees(float deg) {
        deg = deg % 360.0f;
        if (deg < -180.0f) deg += 360.0f;
        if (deg > 180.0f)  deg -= 360.0f;
        return deg;
    }
    private static float lerpYawDegrees(float from, float to, float t) {
        float d = wrapDegrees(to - from);
        return from + d * t;
    }

    private static void runNextTick(ServerLevel s, Runnable r) {
        s.getServer().execute(r);
    }
    private static void runTicksLater(ServerLevel s, int ticks, Runnable r) {
        if (ticks <= 0) { s.getServer().execute(r); return; }
        s.getServer().execute(() -> runTicksLater(s, ticks - 1, r));
    }

    /** Micro-correction to keep entity center safely inside destination lane even if physics nudged it. */
    private static void clampInsideDestination(ServerLevel server, Entity e, Vec3 rDst, Vec3 up, BlockPos tgtAnchor, Direction.Axis axis, int dstW, int dstH) {
        double halfW = e.getBbWidth() / 2.0;
        double minLd = (halfW + WALL_PAD) - 0.5;
        double maxLd = (dstW - 1.0) - ((halfW + WALL_PAD) - 0.5);
        double maxVd = Math.max(0.0, dstH - e.getBbHeight() - CEIL_PAD);

        Vec3 tgtBase = new Vec3(tgtAnchor.getX() + 0.5, tgtAnchor.getY(), tgtAnchor.getZ() + 0.5);
        Vec3 rel     = e.position().subtract(tgtBase);

        double L = rel.dot(rDst);
        double V = rel.dot(up);

        if (minLd <= maxLd) L = Mth.clamp(L, minLd, maxLd);
        V = Mth.clamp(V, 0.0, maxVd);

        Vec3 corrected = tgtBase.add(rDst.scale(L)).add(0, V, 0);

        if (e instanceof ServerPlayer sp) {
            sp.connection.teleport(corrected.x, corrected.y, corrected.z, sp.getYHeadRot(), sp.getXRot());
        } else {
            e.teleportTo(corrected.x, corrected.y, corrected.z);
        }
        e.resetFallDistance();
    }

    // ====== robust exit position finder ======

    /**
     * Search along the outward normal for a position where the entity's AABB doesn't collide.
     * Also tries tiny vertical nudges. Keeps the entity inside the portal lane if everything is blocked.
     */
    private static Vec3 findSafeExit(ServerLevel level, Entity e, Vec3 desired, Vec3 outNormal,
                                     BlockPos tgtAnchor, Vec3 rDst, int dstW, int dstH) {

        // First, if desired is already free, use it.
        if (isFreeAt(level, e, desired)) return desired;

        // Try stepping outward up to ~0.9 blocks ahead.
        for (double d = 0.0625; d <= 0.9375; d += 0.0625) {
            Vec3 cand = desired.add(outNormal.scale(d));
            if (isFreeAt(level, e, cand)) return cand;
        }

        // Try slight vertical adjustments (down a bit, then up), with small outward bias.
        for (double dv = -0.25; dv <= 0.25; dv += 0.0625) {
            Vec3 cand = desired.add(0, dv, 0).add(outNormal.scale(0.0625));
            if (isFreeAt(level, e, cand)) return cand;
        }

        // Everything blocked – fall back to staying just inside the portal skin on the destination side.
        double halfW = e.getBbWidth() / 2.0;
        double minLd = (halfW + WALL_PAD) - 0.5;
        double maxLd = (dstW - 1.0) - ((halfW + WALL_PAD) - 0.5);
        double maxVd = Math.max(0.0, dstH - e.getBbHeight() - CEIL_PAD);

        Vec3 base = new Vec3(tgtAnchor.getX() + 0.5, tgtAnchor.getY(), tgtAnchor.getZ() + 0.5);
        Vec3 rel  = desired.subtract(base);

        double L = rel.dot(rDst);
        double V = rel.y;

        if (minLd <= maxLd) L = Mth.clamp(L, minLd, maxLd);
        V = Mth.clamp(V, 0.0, maxVd);

        Vec3 onSkin = base.add(rDst.scale(L)).add(0, V, 0).add(outNormal.scale(0.03125));
        return onSkin;
    }

    /** AABB collision check at an arbitrary position (without moving the entity yet). */
    private static boolean isFreeAt(ServerLevel level, Entity e, Vec3 pos) {
        Vec3 delta = pos.subtract(e.position());
        AABB moved = e.getBoundingBox().move(delta).inflate(1.0E-4);
        return level.noCollision(e, moved);
    }
}
