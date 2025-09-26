package net.anatomyworld.harambefmod.economy;

import java.util.Locale;

/**
 * Short-scale number formatter for user-facing balances.
 * Examples:
 *   999    -> "999"
 *   1_000  -> "1k"
 *   12_300 -> "12.3k"
 *   1_000_000 -> "1m"
 *   1_234_567 -> "1.23m"
 *   9_000_000_000L -> "9b"
 *   4_200_000_000_000L -> "4.2t"
 *   7_000_000_000_000_000L -> "7q"   (quadrillion)
 *   9_223_372_036_854_775_807L -> "9.22Q" (max long ≈ 9.22 quintillion)
 */
public final class Abbrev {
    private Abbrev() {}

    // Short-scale suffixes up to quintillion (covers full signed long range).
    // k=10^3, m=10^6, b=10^9, t=10^12, q=10^15 (quadrillion), Q=10^18 (quintillion)
    private static final String[] SUFFIX = {"", "k", "m", "b", "t", "q", "Q"};

    /** Format a long using short-scale suffixes with up to 3 significant digits. */
    public static String format(long n) {
        boolean neg = n < 0;
        double v = Math.abs((double) n);
        int idx = 0;

        while (v >= 1000.0 && idx < SUFFIX.length - 1) {
            v /= 1000.0;
            idx++;
        }

        String num;
        if (v >= 100)       num = String.format(Locale.ROOT, "%.0f", v);
        else if (v >= 10)   num = String.format(Locale.ROOT, "%.1f", v);
        else                num = String.format(Locale.ROOT, "%.2f", v);

        // trim trailing zeros
        if (num.indexOf('.') >= 0) {
            while (num.endsWith("0")) num = num.substring(0, num.length() - 1);
            if (num.endsWith("."))    num = num.substring(0, num.length() - 1);
        }

        return (neg ? "-" : "") + num + SUFFIX[idx];
    }
}
