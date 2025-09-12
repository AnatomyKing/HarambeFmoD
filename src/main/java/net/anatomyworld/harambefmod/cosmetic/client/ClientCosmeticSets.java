package net.anatomyworld.harambefmod.cosmetic.client;

import net.anatomyworld.harambefmod.cosmetic.CosmeticSet;
import net.anatomyworld.harambefmod.network.SyncCosmeticSetsPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientCosmeticSets {
    private static final Map<ResourceLocation, CosmeticSet> SETS = new LinkedHashMap<>();

    public static Map<ResourceLocation, CosmeticSet> sets() { return SETS; }
    public static void accept(SyncCosmeticSetsPayload payload) {
        SETS.clear();
        for (var e : payload.entries()) SETS.put(e.id(), e.set());
    }
    private ClientCosmeticSets() {}
}
