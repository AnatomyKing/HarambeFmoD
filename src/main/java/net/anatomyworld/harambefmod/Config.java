// src/main/java/net/anatomyworld/harambefmod/Config.java
package net.anatomyworld.harambefmod;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class Config {
    public static final ModConfigSpec COMMON_SPEC;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> OVERWORLD_LIKE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        OVERWORLD_LIKE = b
                .comment("""
                Dimensions treated as 'overworld-like' for Nether return.
                The last one a player visited will be used when leaving the Nether.
                Include minecraft:overworld and any custom overworld-mimic dims.
                """)
                .defineList("overworldLikeDimensions",
                        List.of("minecraft:overworld", "harambefmod:dynastirium"),
                        o -> o instanceof String s && ResourceLocation.isValidResourceLocation(s));

        COMMON_SPEC = b.build();
    }

    public static boolean isOverworldLike(ResourceKey<Level> dim) {
        String id = dim.location().toString();
        return OVERWORLD_LIKE.get().contains(id);
    }
}
