package net.anatomyworld.harambefmod.worldgen.api;

import java.util.OptionalLong;

public interface SeedOverrideSupport {
    void harambefmod$setSeedOverride(OptionalLong seed);
    OptionalLong harambefmod$getSeedOverride();
}
