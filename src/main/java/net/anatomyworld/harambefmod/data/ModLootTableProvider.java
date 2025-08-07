package net.anatomyworld.harambefmod.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Registers every loot-table sub-provider generated at runData. */
public final class ModLootTableProvider extends LootTableProvider {

    public ModLootTableProvider(PackOutput out,
                                CompletableFuture<HolderLookup.Provider> lookup) {
        super(out,
                Set.of(),                                // no required vanilla tables
                List.of(new SubProviderEntry(            // our single block provider
                        ModBlockLootSubProvider::new,     // ctor ref → (HolderLookup.Provider) -> new …
                        LootContextParamSets.BLOCK)),
                lookup);
    }
}
