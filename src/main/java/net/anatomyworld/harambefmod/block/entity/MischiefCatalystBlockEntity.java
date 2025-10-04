// MischiefCatalystBlockEntity.java
package net.anatomyworld.harambefmod.block.entity;

import net.anatomyworld.harambefmod.block.ModBlocks;
import net.anatomyworld.harambefmod.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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
import net.neoforged.neoforge.event.EventHooks; // for fireBonemealEvent()

/**
 * Uses our OWN BlockEntityType (matches harambefmod:mischief_catalyst),
 * but COMPOSES the vanilla catalyst listener to keep 1:1 spread & bloom.
 */
public class MischiefCatalystBlockEntity extends BlockEntity
        implements GameEventListener.Provider<SculkCatalystBlockEntity.CatalystListener> {

    // Sweep where sculk could appear this tick
    private static final int SWAP_RADIUS = 16;

    // Chance to auto-bonemeal a newly-placed MISCHIEF_GRASS_BLOCK (0.0..1.0)
    private static final float AUTO_BONEMEAL_CHANCE = 0.25f;

    // vanilla listener (adds cursors on ENTITY_DIE, does bloom, etc.)
    private final SculkCatalystBlockEntity.CatalystListener listener;

    public MischiefCatalystBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MISCHIEF_CATALYST_ENTITY.get(), pos, state);
        PositionSource src = new BlockPositionSource(pos);
        // identical to vanilla: radius=8, bloom timing, XP→charges, etc.
        this.listener = new SculkCatalystBlockEntity.CatalystListener(state, src);
    }

    // vanilla stores spreader cursors in NBT; keep that 1:1
    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        listener.getSculkSpreader().load(in);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        listener.getSculkSpreader().save(out);
        super.saveAdditional(out);
    }

    @Override
    public SculkCatalystBlockEntity.CatalystListener getListener() {
        return listener;
    }

    /** Tick: run vanilla spread update, then translate SCULK → Mischief blocks. */
    public static void serverTick(Level level, BlockPos pos, BlockState state, MischiefCatalystBlockEntity be) {
        if (level.isClientSide) return;

        // 1) exact vanilla logic (radius 8, charge/cursor rules, etc.)
        be.listener.getSculkSpreader().updateCursors(level, pos, level.getRandom(), true);

        // 2) translate placed blocks to your variants (preserve faces + waterlogging)
        ServerLevel srv = (ServerLevel) level;
        BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
        int r = SWAP_RADIUS;
        RandomSource rnd = srv.getRandom();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    cur.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    BlockState s = srv.getBlockState(cur);

                    // Solid sculk → mischief grass block
                    if (s.is(Blocks.SCULK)) {
                        srv.setBlock(cur, ModBlocks.MISCHIEF_GRASS_BLOCK.get().defaultBlockState(), 3);

                        // (optional/random) trigger YOUR Bonemeal handler on that grass
                        // We do it by firing the official Bonemeal event; your existing
                        // CustomGrassSelfBonemealHandler will catch it and do the placement.
                        if (rnd.nextFloat() < AUTO_BONEMEAL_CHANCE) {
                            fireBonemealForMischiefGrass(srv, cur);
                        }
                        continue;
                    }

                    // Sculk vein → mischief vein (copy faces + WATERLOGGED)
                    if (s.is(Blocks.SCULK_VEIN)) {
                        BlockState out = ModBlocks.MISCHIEF_VEIN.get().defaultBlockState();

                        for (Direction d : Direction.values()) {
                            var face = MultifaceBlock.getFaceProperty(d);
                            if (face != null && s.hasProperty(face) && s.getValue(face)) out = out.setValue(face, true);
                        }
                        if (s.hasProperty(BlockStateProperties.WATERLOGGED) && out.hasProperty(BlockStateProperties.WATERLOGGED)) {
                            out = out.setValue(BlockStateProperties.WATERLOGGED, s.getValue(BlockStateProperties.WATERLOGGED));
                        }
                        srv.setBlock(cur, out, 3);
                    }
                }
            }
        }

        // 3) Ensure NO vein faces attach to the catalyst itself
        stripVeinFacesFromCatalyst(srv, pos);
    }

    /** Fires the standard Bonemeal event on a MISCHIEF_GRASS_BLOCK at {@code pos}. */
    private static void fireBonemealForMischiefGrass(ServerLevel level, BlockPos pos) {
        BlockState now = level.getBlockState(pos);
        if (!now.is(ModBlocks.MISCHIEF_GRASS_BLOCK.get())) return;

        // Use a dummy stack; your handler will shrink it if player is null (harmless).
        ItemStack fakeBoneMeal = new ItemStack(Items.BONE_MEAL);
        EventHooks.fireBonemealEvent(null, level, pos, now, fakeBoneMeal); // posts to NeoForge.EVENT_BUS. :contentReference[oaicite:1]{index=1}
    }

    /** Remove any MISCHIEF_VEIN faces that are attached TO the catalyst block. */
    private static void stripVeinFacesFromCatalyst(ServerLevel level, BlockPos catalystPos) {
        for (Direction d : Direction.values()) {
            BlockPos neighbor = catalystPos.relative(d);
            BlockState s = level.getBlockState(neighbor);
            if (!s.is(ModBlocks.MISCHIEF_VEIN.get())) continue;

            // On the NEIGHBOR, the face pointing back to the catalyst is opposite(d)
            var faceProp = MultifaceBlock.getFaceProperty(d.getOpposite());
            if (faceProp != null && s.hasProperty(faceProp) && s.getValue(faceProp)) {
                BlockState newState = s.setValue(faceProp, false);

                // If no faces remain, delete the vein block entirely
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
