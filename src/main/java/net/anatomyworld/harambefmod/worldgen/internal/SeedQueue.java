package net.anatomyworld.harambefmod.worldgen.internal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.OptionalLong;

public final class SeedQueue {
    private static final ThreadLocal<Deque<OptionalLong>> Q =
            ThreadLocal.withInitial(ArrayDeque::new);

    private SeedQueue() {}

    public static void push(OptionalLong seed) { Q.get().addLast(seed); }

    public static OptionalLong poll() {
        Deque<OptionalLong> q = Q.get();
        return q.isEmpty() ? OptionalLong.empty() : q.pollFirst();
    }

    public static void clear() { Q.get().clear(); }
}
