package net.anatomyworld.harambefmod.worldgen;

import java.util.List;
import java.util.Map;

public final class ProfileFeatureSets {
    private ProfileFeatureSets() {}

    /** Which feature “bases” to inject per profile suffix. */
    public static final Map<String, List<String>> BY_SUFFIX = Map.of(
            PatchProfiles.OVERWORLDNEW.suffix(), List.of(
                    "swap_short_grass",
                    "patch_tree"
            ),
            PatchProfiles.BELMONTNEW.suffix(), List.of(
                    "swap_short_grass",
                    "patch_tree"
            ),
            PatchProfiles.DYNASTYNEW.suffix(), List.of(
                    "swap_short_grass",
                    "patch_tree"
            ),
            PatchProfiles.IMPERIUMNEW.suffix(), List.of(
                    "swap_short_grass",
                    "patch_tree"
            ),
            PatchProfiles.MISCHIEFNEW.suffix(), List.of(
                    "swap_short_grass",
                    "patch_tree"
            )
    );
}
