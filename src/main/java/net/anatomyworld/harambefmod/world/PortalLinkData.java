package net.anatomyworld.harambefmod.world;

import com.mojang.serialization.Codec;
import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Links a hex code to 1 pending endpoint or a linked pair. */
public final class PortalLinkData extends SavedData {

    /* ---------- SavedDataType & Codec (1.21.8) ---------- */

    /** Encode to a CompoundTag, decode back to PortalLinkData. */
    public static final Codec<PortalLinkData> CODEC = CompoundTag.CODEC.xmap(
            tag -> {
                PortalLinkData d = new PortalLinkData();
                d.loadFromTag(tag);
                return d;
            },
            d -> d.saveToTag(new CompoundTag())
    );

    /** World save id + how to construct & serialize. */
    public static final SavedDataType<PortalLinkData> TYPE =
            new SavedDataType<>(HarambeCore.MOD_ID + "_portal_links", PortalLinkData::new, CODEC);

    /** Grab or create for this ServerLevel. */
    public static PortalLinkData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    /* ---------- Model ---------- */

    /** One rectangular portal endpoint in a dimension. */
    public static final class Endpoint {
        public final ResourceKey<Level> dim;
        public final BlockPos anchor;            // interior bottom-left
        public final Direction.Axis axis;
        public final int width, height;
        public final int color;                  // 0xRRGGBB
        public final Direction front;            // saved "front"

        public Endpoint(ResourceKey<Level> dim, BlockPos anchor, Direction.Axis axis,
                        int width, int height, int color, Direction front) {
            this.dim = dim; this.anchor = anchor; this.axis = axis;
            this.width = width; this.height = height; this.color = color; this.front = front;
        }

        public CompoundTag save() {
            CompoundTag t = new CompoundTag();
            t.putString("dim", dim.location().toString());
            t.putInt("ax", anchor.getX());
            t.putInt("ay", anchor.getY());
            t.putInt("az", anchor.getZ());
            t.putString("axis", axis.getName()); // "x" or "z"
            t.putInt("w", width);
            t.putInt("h", height);
            t.putInt("color", color);
            t.putString("front", front.getName());
            return t;
        }

        public static Endpoint load(CompoundTag t) {
            String dimStr = t.getStringOr("dim", "minecraft:overworld");
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimStr));

            int ax = t.getIntOr("ax", 0);
            int ay = t.getIntOr("ay", 0);
            int az = t.getIntOr("az", 0);
            BlockPos anchor = new BlockPos(ax, ay, az);

            String axisStr = t.getStringOr("axis", "x");
            Direction.Axis axis = "x".equalsIgnoreCase(axisStr) ? Direction.Axis.X : Direction.Axis.Z;

            int w = t.getIntOr("w", 1);
            int h = t.getIntOr("h", 1);
            int c = t.getIntOr("color", 0xFFFFFF);

            String frStr = t.getStringOr("front", "south");
            Direction fr = Direction.byName(frStr);
            if (fr == null || fr.getAxis() == Direction.Axis.Y) fr = Direction.SOUTH;

            return new Endpoint(dim, anchor, axis, w, h, c, fr);
        }
    }

    private static final class Link {
        Endpoint a;
        Endpoint b; // null until linked

        Link(Endpoint a) { this.a = a; }
        boolean linked() { return b != null; }

        CompoundTag save() {
            CompoundTag t = new CompoundTag();
            t.put("a", a.save());
            if (b != null) t.put("b", b.save());
            return t;
        }

        static Link load(CompoundTag t) {
            // 'a' must exist; 'b' is optional
            Link l = new Link(Endpoint.load(t.getCompoundOrEmpty("a")));
            t.getCompound("b").ifPresent(bTag -> l.b = Endpoint.load(bTag));
            return l;
        }
    }

    private final Map<String, Link> links = new HashMap<>();

    public PortalLinkData() {}

    /* ---------- Persistence helpers ---------- */

    private void loadFromTag(CompoundTag tag) {
        links.clear();
        for (String key : tag.keySet()) {
            tag.getCompound(key).ifPresent(sub -> links.put(key, Link.load(sub)));
        }
    }

    private CompoundTag saveToTag(CompoundTag out) {
        for (var e : links.entrySet()) out.put(e.getKey(), e.getValue().save());
        return out;
    }

    /* ---------- API ---------- */

    /** 0 = first endpoint (pending), 1 = linked, -1 = code already used by two endpoints. */
    public int registerOrLink(ServerLevel level, BananaPortalShape.Frame f, String hexUpper, int rgb, Direction front) {
        Link l = links.get(hexUpper);
        Endpoint ep = new Endpoint(level.dimension(), f.anchor(), f.axis(), f.width(), f.height(), rgb, front);

        if (l == null) { links.put(hexUpper, new Link(ep)); setDirty(); return 0; }
        if (!l.linked()) { l.b = ep; setDirty(); return 1; }
        return -1;
    }

    /** Find the other side for any interior position. */
    public Optional<Endpoint> findOtherEndpointForPosition(ServerLevel level, BlockPos interior) {
        for (var e : links.values()) {
            if (!e.linked()) continue;
            if (belongsTo(level, interior, e.a)) return Optional.of(e.b);
            if (belongsTo(level, interior, e.b)) return Optional.of(e.a);
        }
        return Optional.empty();
    }

    private static boolean belongsTo(ServerLevel level, BlockPos p, Endpoint ep) {
        if (!level.dimension().equals(ep.dim)) return false;
        Direction right = (ep.axis == Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;
        BlockPos end = ep.anchor.relative(right, ep.width - 1).above(ep.height - 1);
        return p.getX() >= Math.min(ep.anchor.getX(), end.getX()) && p.getX() <= Math.max(ep.anchor.getX(), end.getX())
                && p.getY() >= ep.anchor.getY() && p.getY() <= end.getY()
                && p.getZ() >= Math.min(ep.anchor.getZ(), end.getZ()) && p.getZ() <= Math.max(ep.anchor.getZ(), end.getZ());
    }

    /** Remove this endpoint’s interior; keep the other side pending if it existed. */
    public void removePortalAt(ServerLevel level, BlockPos interior, boolean clearBlocks) {
        String hitKey = null; boolean hitWasA = false; Link hit = null;

        for (var e : links.entrySet()) {
            var l = e.getValue();
            if (belongsTo(level, interior, l.a)) { hitKey = e.getKey(); hit = l; hitWasA = true; break; }
            if (l.linked() && belongsTo(level, interior, l.b)) { hitKey = e.getKey(); hit = l; hitWasA = false; break; }
        }
        if (hit == null) return;

        Endpoint ep = hitWasA ? hit.a : hit.b;
        if (clearBlocks) clearInterior(level, ep);

        if (hit.linked()) {
            Endpoint keep = hitWasA ? hit.b : hit.a;
            links.put(hitKey, new Link(keep));
        } else {
            links.remove(hitKey);
        }
        setDirty();
    }

    private static void clearInterior(ServerLevel level, Endpoint ep) {
        Direction right = (ep.axis == Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;
        for (int y = 0; y < ep.height; y++) {
            for (int x = 0; x < ep.width; x++) {
                BlockPos ip = ep.anchor.relative(right, x).above(y);
                if (level.getBlockState(ip).is(ModBlocks.BANANA_PORTAL.get())) {
                    level.setBlock(ip, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    /* Note: We don't need to override SavedData#save(...) here because we serialize via TYPE's Codec. */
}
