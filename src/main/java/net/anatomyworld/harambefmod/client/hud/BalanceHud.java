package net.anatomyworld.harambefmod.client.hud;

import com.mojang.blaze3d.platform.NativeImage;
import net.anatomyworld.harambefmod.HarambeCore;
import net.anatomyworld.harambefmod.client.sync.ClientBalance;
import net.anatomyworld.harambefmod.economy.Abbrev;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Ultra-optimized Balance HUD (NeoForge 1.21.8)
 * - No per-frame allocations on the hot path
 * - Precomputed RLs and PNG paths for digits/letters/symbols
 * - Glyph metadata cached per character & reused
 * - Text run cached; recomputed only when balance string actually changes
 * - Instant water bump (sticky until air full), exact extra mount rows
 * - Hidden in spectator & creative
 */
public final class BalanceHud {
    private BalanceHud() {}

    /* ------------------- layout constants ------------------- */

    private static final int ICON_W = 9, ICON_H = 9; // pearl.png is 9x9
    private static final int GLYPH_H = 9;            // glyph PNGs are 8–9px tall
    private static final int PAD_RIGHT = 230;
    private static final int GAP_ICON_TEXT = 2;
    private static final int MIN_W = 3, MAX_W = 7;
    private static final int HUD_ROW = 10;

    /* ------------------- precomputed sprites ------------------- */

    // Logical sprite base: assets/<modid>/textures/gui/sprites/...
    private static final String BASE = "textures/gui/sprites/";
    private static final String MOD = HarambeCore.MOD_ID;

    // Digits 0..9
    private static final ResourceLocation[] DIGIT_PNG = new ResourceLocation[10];
    // Letters a..z
    private static final ResourceLocation[] LETTER_PNG = new ResourceLocation[26];
    // Symbols
    private static final ResourceLocation PERIOD_PNG =
            rlPng("hud/symbols/period");
    private static final ResourceLocation COMMA_PNG  =
            rlPng("hud/symbols/comma");
    private static final ResourceLocation APOST_PNG  =
            rlPng("hud/symbols/apostrophe");

    // Icon
    private static final ResourceLocation ICON_PNG =
            rlPng("hud/pearl");

    static {
        for (int d = 0; d < 10; d++) {
            DIGIT_PNG[d] = rlPng("hud/numbers/" + (char) ('0' + d));
        }
        for (int i = 0; i < 26; i++) {
            LETTER_PNG[i] = rlPng("hud/letters/" + (char) ('a' + i));
        }
    }

    private static ResourceLocation rlPng(String pathNoExt) {
        return ResourceLocation.fromNamespaceAndPath(MOD, BASE + pathNoExt + ".png");
    }

    /* ------------------- glyph metadata cache ------------------- */

    private record GlyphMeta(ResourceLocation png, int texW, int texH, int drawW) {}
    private static final Map<ResourceLocation, GlyphMeta> META = new HashMap<>();

    // Per-character quick caches (avoid map lookups in hot path)
    private static final GlyphMeta[] DIGIT_META  = new GlyphMeta[10];
    private static final GlyphMeta[] LETTER_META = new GlyphMeta[26];
    private static GlyphMeta PERIOD_META, COMMA_META, APOST_META;

    private static GlyphMeta gmDigit(int d) {
        GlyphMeta m = DIGIT_META[d];
        if (m == null) DIGIT_META[d] = m = metaFor(DIGIT_PNG[d]);
        return m;
    }
    private static GlyphMeta gmLetter(int i) {
        GlyphMeta m = LETTER_META[i];
        if (m == null) LETTER_META[i] = m = metaFor(LETTER_PNG[i]);
        return m;
    }
    private static GlyphMeta gmPeriod() { return PERIOD_META == null ? (PERIOD_META = metaFor(PERIOD_PNG)) : PERIOD_META; }
    private static GlyphMeta gmComma()  { return COMMA_META  == null ? (COMMA_META  = metaFor(COMMA_PNG))  : COMMA_META; }
    private static GlyphMeta gmApost()  { return APOST_META  == null ? (APOST_META  = metaFor(APOST_PNG))  : APOST_META; }

    private static GlyphMeta metaFor(ResourceLocation png) {
        GlyphMeta cached = META.get(png);
        if (cached != null) return cached;

        int texW = 8, texH = GLYPH_H, drawW = 6; // defaults
        try {
            var opt = Minecraft.getInstance().getResourceManager().getResource(png);
            if (opt.isPresent()) {
                try (InputStream in = opt.get().open(); NativeImage img = NativeImage.read(in)) {
                    texW = img.getWidth();
                    texH = img.getHeight();
                    int scanH = Math.min(texH, GLYPH_H);

                    int rightmost = -1;
                    for (int x = texW - 1; x >= 0; x--) {
                        boolean anyOpaque = false;
                        for (int y = 0; y < scanH; y++) {
                            int argb = img.getPixel(x, y);
                            int a = (argb >>> 24) & 0xFF;
                            if (a > 0) { anyOpaque = true; break; }
                        }
                        if (anyOpaque) { rightmost = x; break; }
                    }
                    drawW = rightmost >= 0 ? (rightmost + 1) : 0;
                }
            }
        } catch (IOException ignored) {}

        if (drawW > 0) drawW = Math.max(MIN_W, Math.min(MAX_W, drawW));

        GlyphMeta meta = new GlyphMeta(png, texW, texH, drawW);
        META.put(png, meta);
        return meta;
    }

    /* ------------------- cached text run ------------------- */

    // Stores a compiled line ready for blitting without allocations.
    private static final class Run {
        long lastBalance = Long.MIN_VALUE;
        String text = "";           // human text (lowercased)
        char[] chars = new char[0]; // char array view of text
        GlyphMeta[] metas = new GlyphMeta[0]; // glyph metas aligned with chars
        int[] advances = new int[0];          // per-glyph advance (drawW or space)
        int width = 0;              // total text width (sum of advances)
    }
    private static final Run RUN = new Run();

    private static void ensureRunUpToDate(long balance) {
        if (balance == RUN.lastBalance) return;

        // Format only on value change (rare). Abbrev is fine here.
        String txt = Abbrev.format(balance).toLowerCase(Locale.ROOT);
        int n = txt.length();

        // (re)size arrays only if needed
        if (RUN.chars.length < n) {
            RUN.chars = new char[n];
            RUN.metas = new GlyphMeta[n];
            RUN.advances = new int[n];
        }

        txt.getChars(0, n, RUN.chars, 0);

        int width = 0;
        for (int i = 0; i < n; i++) {
            char ch = RUN.chars[i];
            GlyphMeta meta;
            int adv;

            if (ch == ' ') {
                meta = null;
                adv = 4;
            } else if (ch >= '0' && ch <= '9') {
                meta = gmDigit(ch - '0');
                adv = meta.drawW;
            } else if (ch >= 'a' && ch <= 'z') {
                meta = gmLetter(ch - 'a');
                adv = meta.drawW;
            } else if (ch == '.') {
                meta = gmPeriod();
                adv = meta.drawW;
            } else if (ch == ',') {
                meta = gmComma();
                adv = meta.drawW;
            } else if (ch == '\'') {
                meta = gmApost();
                adv = meta.drawW;
            } else {
                // unsupported character -> skip w/ zero advance
                meta = null;
                adv = 0;
            }

            RUN.metas[i] = meta;
            RUN.advances[i] = adv;
            width += adv;
        }

        RUN.text = txt;
        RUN.width = width;
        RUN.lastBalance = balance;
    }

    /* ------------------- registration ------------------- */

    public static void registerLayers(RegisterGuiLayersEvent e) {
        e.registerAbove(
                VanillaGuiLayers.AIR_LEVEL,
                ResourceLocation.fromNamespaceAndPath(MOD, "balance_hud"),
                BalanceHud::render
        );
    }

    /* ------------------- render ------------------- */

    // 1.21+: (GuiGraphics, DeltaTracker)
    private static void render(GuiGraphics gg, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options.hideGui) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        // Hide in spectator AND creative
        if (p.isSpectator() || p.isCreative()) return;

        // Prepare run once per balance change (no per-frame formatting/measurement)
        long bal = ClientBalance.get();
        ensureRunUpToDate(bal);

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        int y = computeHudY(p, sh);

        int totalW = ICON_W + GAP_ICON_TEXT + RUN.width;
        int x = sw - PAD_RIGHT - totalW;

        // Icon
        GlyphMeta icon = metaFor(ICON_PNG); // cached after first read
        blitFull(gg, icon.png, x, y, ICON_W, ICON_H, icon.texW, icon.texH);

        // Text baseline and blits (no allocations; pre-measured advances)
        int penX = x + ICON_W + GAP_ICON_TEXT;
        int baseline = y + Math.max(0, (ICON_H - GLYPH_H) / 2);

        final char[] chars = RUN.chars;
        final GlyphMeta[] metas = RUN.metas;
        final int[] adv = RUN.advances;
        final int n = RUN.text.length();

        for (int i = 0; i < n; i++) {
            if (chars[i] == ' ') { penX += 4; continue; }
            GlyphMeta m = metas[i];
            if (m == null || m.drawW <= 0) continue;
            blitFull(gg, m.png, penX, baseline, m.drawW, GLYPH_H, m.texW, m.texH);
            penX += adv[i];
        }
    }

    /* ------------------- vertical placement ------------------- */

    private static int computeHudY(LocalPlayer p, int screenH) {
        // Base: like vanilla hunger line (one band up from hotbar)
        int base = screenH - 39 - HUD_ROW;

        int shiftUp = 0;

        // Water bump: instant up; sticky while air not full
        boolean eyesInWater = p.isEyeInFluid(FluidTags.WATER);
        boolean airNotFull  = p.getAirSupply() < p.getMaxAirSupply();
        if (eyesInWater || airNotFull) {
            shiftUp += HUD_ROW;
        }

        // Mount hearts: bump only EXTRA rows beyond the first
        int rows = vehicleHeartRows(p);
        if (rows > 1) shiftUp += (rows - 1) * HUD_ROW;

        return Math.max(2, base - shiftUp - 1);
    }

    /**
     * Mount rows like vanilla:
     * halfHearts = ceil(maxHp / 2); rows = max(1, ceil(halfHearts / 10)).
     * Returns 0 if not riding a LivingEntity or maxHp <= 0.
     */
    private static int vehicleHeartRows(LocalPlayer p) {
        if (!(p.getVehicle() instanceof LivingEntity le)) return 0;
        float maxHp = le.getMaxHealth();
        if (maxHp <= 0f) return 0;
        int halfHearts = Mth.ceil(maxHp / 2f);
        return Math.max(1, Mth.ceil(halfHearts / 10f));
    }

    /* ------------------- blit helper ------------------- */

    private static void blitFull(GuiGraphics gg, ResourceLocation texPng, int x, int y,
                                 int drawW, int drawH, int texW, int texH) {
        // GuiGraphics is the standard HUD API in modern Minecraft. (1.21+)
        gg.blit(RenderPipelines.GUI_TEXTURED, texPng, x, y, 0f, 0f, drawW, drawH, texW, texH);
    }
}
