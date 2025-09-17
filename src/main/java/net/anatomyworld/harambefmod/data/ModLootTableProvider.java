// src/main/java/net/anatomyworld/harambefmod/data/ModLootTableProvider.java
package net.anatomyworld.harambefmod.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput out, CompletableFuture<HolderLookup.Provider> lookup) {
        super(out,
                Set.of(), // required tables
                List.of(
                        // Block loot: function expects HolderLookup.Provider -> SubProvider
                        new SubProviderEntry(ModBlockLootSubProvider::new, LootContextParamSets.BLOCK),
                        // Gameplay loot: no lookup needed, still must be a Function<HolderLookup.Provider, ...>
                        new SubProviderEntry((prov) -> new ModGameplayLootSubProvider(), LootContextParamSets.EMPTY)
                ),
                lookup
        );
    }
}
