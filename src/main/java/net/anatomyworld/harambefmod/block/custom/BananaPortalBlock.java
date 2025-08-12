package net.anatomyworld.harambefmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.anatomyworld.harambefmod.block.entity.BananaPortalBlockEntity;
import net.anatomyworld.harambefmod.world.BananaPortalShape;
import net.anatomyworld.harambefmod.world.PortalLinkData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Thin, panel-like portal block with re-entry teleport and saved front direction. */
public final class BananaPortalBlock extends Block implements EntityBlock {
    public static final MapCodec<BananaPortalBlock> CODEC = BlockBehaviour.simpleCodec(props -> new BananaPortalBlock());

    /** X = portal plane perpendicular to X (normal along ±Z). Z = portal plane perpendicular to Z (normal along ±X). */
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    // 2px thin panel around the center plane
    private static final VoxelShape SHAPE_X = Block.box(0, 0, 7, 16, 16, 9); // normal along Z (portal plane is X)
    private static final VoxelShape SHAPE_Z = Block.box(7, 0, 0, 9, 16, 16); // normal along X (portal plane is Z);

    @Override public @NotNull MapCodec<? extends Block> codec() { return CODEC; }

    public BananaPortalBlock() {
        super(BlockBehaviour.Properties.of()
                .noOcclusion()
                .noCollission()
                // Unbreakable in survival; creative can delete instantly (like bedrock behavior)
                .strength(-1.0F, 3_600_000.0F)
                .lightLevel(s -> 11)
                .sound(SoundType.GLASS)
                .noLootTable()
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(AXIS);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BananaPortalBlockEntity(pos, state);
    }

    /* Outline/camera shape: thin panel matching AXIS */
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return state.getValue(AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    /* Entities can stand in the portal; teleport only on re-entry (not while lingering). */
    @Override
    public void entityInside(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
        if (level.isClientSide) return;
        ServerLevel server = (ServerLevel) level;

        // Use a short cooldown to prevent chain teleports across both portals within the same moment.
        var tag = entity.getPersistentData();
        long now = server.getGameTime();
        long cdUntil = tag.getLong("harambefmod:portal_cd");
        if (cdUntil > now) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BananaPortalBlockEntity portalBE)) return;

        // Determine if the entity JUST ENTERED this portal region this tick:
        long lastTick = tag.getLong("harambefmod:last_portal_tick");
        long lastAnchor = tag.getLong("harambefmod:last_portal_anchor");
        long thisAnchor = portalBE.getAnchor().asLong();

        boolean justEntered = (lastTick != (now - 1)) || (lastAnchor != thisAnchor);

        // Record presence for next tick
        tag.putLong("harambefmod:last_portal_tick", now);
        tag.putLong("harambefmod:last_portal_anchor", thisAnchor);

        if (!justEntered) return; // standing inside; no teleport

        // Find the linked endpoint for this interior
        PortalLinkData data = PortalLinkData.get(server);
        var targetOpt = data.findOtherEndpointForPosition(server, pos);
        if (targetOpt.isEmpty()) return;

        var target = targetOpt.get();
        // Same-dimension only by design (your request)
        if (server.dimension() != target.dim) return;

        // We place you on the other side preserving your lateral offset relative to the source plane,
        // and set your facing to "away" from the destination portal's FRONT direction.
        var sourceAxis = state.getValue(AXIS);              // axis at the block you touched
        var targetAxis = target.axis;                       // axis stored for the destination endpoint

        // Compute lateral offset in source portal local coordinates
        var right = (sourceAxis == Direction.Axis.X) ? Direction.EAST : Direction.SOUTH; // right along interior rows
        var up    = Direction.UP;

        // Recover source region bottom-left interior from the BE anchor + axis (anchor is interior bottom-left)
        BlockPos srcAnchor = portalBE.getAnchor();
        double dx = entity.getX() - (srcAnchor.getX() + 0.5);
        double dy = entity.getY() - (srcAnchor.getY() + 0.0);
        double dz = entity.getZ() - (srcAnchor.getZ() + 0.5);

        // Project onto right & up
        double lateral, vertical;
        if (sourceAxis == Direction.Axis.X) {
            // right=+X, up=+Y
            lateral = dx;
        } else {
            // axis Z: right=+Z, up=+Y
            lateral = dz;
        }
        vertical = dy;

        // Build target center from its anchor and apply the same offset
        BlockPos tgtAnchor = target.anchor;
        double tx, ty, tz;
        if (targetAxis == Direction.Axis.X) {
            // interior coordinates: right=+X, up=+Y, normal=±Z
            tx = tgtAnchor.getX() + 0.5 + lateral;
            ty = tgtAnchor.getY() + vertical;
            tz = tgtAnchor.getZ() + 0.5;
        } else {
            // axis Z: right=+Z, up=+Y, normal=±X
            tx = tgtAnchor.getX() + 0.5;
            ty = tgtAnchor.getY() + vertical;
            tz = tgtAnchor.getZ() + 0.5 + lateral;
        }

        // Drop you exactly in the portal plane; re-entry logic prevents immediate ping-pong.
        entity.teleportTo(tx, ty, tz);

        // Face AWAY from the destination portal's FRONT
        Direction faceAway = target.front.getOpposite();
        entity.setYRot(faceAway.toYRot());   // yaw only
        // keep existing pitch:
        entity.setXRot(entity.getXRot());

        // small cooldown so walking between portals in the same tick won't re-trigger
        tag.putLong("harambefmod:portal_cd", now + 8L);
    }

    /* If frame gets invalidated, purge the whole interior and free the code. */
    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                @NotNull Block neighbor, @NotNull BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;
        ServerLevel server = (ServerLevel) level;
        if (!BananaPortalShape.isInteriorStillFramed(server, pos)) {
            PortalLinkData.get(server).removePortalAt(server, pos, true);
        }
    }

    /* Creative: breaking any interior block clears the whole interior. Survival cannot break this block. */
    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide && player.isCreative()) {
            PortalLinkData.get((ServerLevel) level).removePortalAt((ServerLevel) level, pos, true);
        }
        super.playerWillDestroy(level, pos, state, player);
        return state;
    }
}
