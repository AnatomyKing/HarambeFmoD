package net.anatomyworld.harambefmod.client.sync;

/** Tiny client-side cache used by the HUD. */
public final class ClientBalance {
    private static volatile long BALANCE;
    private ClientBalance() {}

    public static long get() { return BALANCE; }
    public static void set(long value) { BALANCE = Math.max(0L, value); }
}
