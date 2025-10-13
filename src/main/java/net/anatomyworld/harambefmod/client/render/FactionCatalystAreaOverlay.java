package net.anatomyworld.harambefmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.anatomyworld.harambefmod.block.entity.FactionCatalystBlockEntity;
import net.anatomyworld.harambefmod.faction.CatalystRegistry;
import net.anatomyworld.harambefmod.faction.Faction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashMap;
import java.util.Map;

/** Structure-block-like outline for all faction catalysts (colored by faction). */
public final class FactionCatalystAreaOverlay {
    private static final int VIEW_DISTANCE = 128;

    private static final Map<ResourceKey<Level>, LongOpenHashSet> TRACKED = new HashMap<>();

    private FactionCatalystAreaOverlay() {}

    public static void track(ResourceKey<Level> dim, BlockPos pos) {
        TRACKED.computeIfAbsent(dim, k -> new LongOpenHashSet()).add(pos.asLong());
    }
    public static void untrack(ResourceKey<Level> dim, BlockPos pos) {
        LongOpenHashSet set = TRACKED.get(dim);
        if (set != null) set.remove(pos.asLong());
    }

    /** Register on the game bus: NeoForge.EVENT_BUS.addListener(FactionCatalystAreaOverlay::onRenderAfterBlockEntities) */
    public static void onRenderAfterBlockEntities(RenderLevelStageEvent.AfterBlockEntities e) {
        final Level level = e.getLevel();
        final LongOpenHashSet set = TRACKED.get(level.dimension());
        if (set == null || set.isEmpty()) return;

        final PoseStack pose = e.getPoseStack();
        final Vec3 cam = e.getCamera().getPosition();
        final double maxDistSqr = (double) VIEW_DISTANCE * VIEW_DISTANCE;

        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        final int minYWorld = level.getMinY();
        final int maxYWorld = level.getMaxY();

        for (LongIterator it = set.iterator(); it.hasNext();) {
            final BlockPos pos = BlockPos.of(it.nextLong());
            if (pos.distToCenterSqr(cam.x, cam.y, cam.z) > maxDistSqr) continue;

            // Find faction by checking the client-side BE at this position (fallback white if unknown)
            int rgb = 0xFFFFFF;
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FactionCatalystBlockEntity fbe) {
                rgb = colorForFaction(fbe.faction());
            }

            double minX = -CatalystRegistry.RADIUS;
            double minZ = -CatalystRegistry.RADIUS;
            double maxX =  CatalystRegistry.RADIUS_PLUS_ONE;
            double maxZ =  CatalystRegistry.RADIUS_PLUS_ONE;
            double minY = (minYWorld - pos.getY());
            double maxY = (maxYWorld - pos.getY());
            AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

            pose.pushPose();
            pose.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
            // Slightly translucent so it’s not overpowering
            drawBoxEdges(pose, lines, box, rgb, 220);
            pose.popPose();
        }
    }

    private static int colorForFaction(Faction f) {
        // Requested colors:
        // belmont:  #14B002
        // dynasty:  #A10E0F
        // imperium: #2C55A7
        // mischief: #7E2870
        return switch (f) {
            case BELMONT  -> 0x14B002;
            case DYNASTY  -> 0xA10E0F;
            case IMPERIUM -> 0x2C55A7;
            case MISCHIEF -> 0x7E2870;
        };
    }

    private static void line(PoseStack pose, VertexConsumer vc,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2,
                             int r, int g, int b, int a) {
        vc.addVertex(pose.last().pose(), (float) x1, (float) y1, (float) z1)
                .setColor(r, g, b, a)
                .setNormal(pose.last(), 0f, 1f, 0f);
        vc.addVertex(pose.last().pose(), (float) x2, (float) y2, (float) z2)
                .setColor(r, g, b, a)
                .setNormal(pose.last(), 0f, 1f, 0f);
    }

    private static void drawBoxEdges(PoseStack pose, VertexConsumer vc, AABB b, int rgb, int a) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int bl = rgb & 0xFF;

        double x0 = b.minX, x1 = b.maxX;
        double y0 = b.minY, y1 = b.maxY;
        double z0 = b.minZ, z1 = b.maxZ;

        // bottom
        line(pose, vc, x0, y0, z0, x1, y0, z0, r, g, bl, a);
        line(pose, vc, x1, y0, z0, x1, y0, z1, r, g, bl, a);
        line(pose, vc, x1, y0, z1, x0, y0, z1, r, g, bl, a);
        line(pose, vc, x0, y0, z1, x0, y0, z0, r, g, bl, a);

        // top
        line(pose, vc, x0, y1, z0, x1, y1, z0, r, g, bl, a);
        line(pose, vc, x1, y1, z0, x1, y1, z1, r, g, bl, a);
        line(pose, vc, x1, y1, z1, x0, y1, z1, r, g, bl, a);
        line(pose, vc, x0, y1, z1, x0, y1, z0, r, g, bl, a);

        // verticals
        line(pose, vc, x0, y0, z0, x0, y1, z0, r, g, bl, a);
        line(pose, vc, x1, y0, z0, x1, y1, z0, r, g, bl, a);
        line(pose, vc, x1, y0, z1, x1, y1, z1, r, g, bl, a);
        line(pose, vc, x0, y0, z1, x0, y1, z1, r, g, bl, a);
    }
}
