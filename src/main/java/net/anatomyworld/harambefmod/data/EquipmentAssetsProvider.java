package net.anatomyworld.harambefmod.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.anatomyworld.harambefmod.HarambeCore;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class EquipmentAssetsProvider implements DataProvider {
    private final PackOutput output;
    public EquipmentAssetsProvider(PackOutput output) { this.output = output; }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(
                saveSet(cache, "belmont"),
                saveSet(cache, "dynasty"),
                saveSet(cache, "imperium"),
                saveSet(cache, "mischief")
        );
    }

    private CompletableFuture<?> saveSet(CachedOutput cache, String name) {
        // texture ids: harambefmod:<name>
        String texId = HarambeCore.MOD_ID + ":" + name;

        JsonArray humanoid = new JsonArray();
        JsonObject humLayer = new JsonObject();
        humLayer.addProperty("texture", texId); // -> textures/entity/equipment/humanoid/<name>.png
        humanoid.add(humLayer);

        JsonArray leggings = new JsonArray();
        JsonObject legLayer = new JsonObject();
        legLayer.addProperty("texture", texId); // -> textures/entity/equipment/humanoid_leggings/<name>.png
        leggings.add(legLayer);

        JsonObject layers = new JsonObject();
        layers.add("humanoid", humanoid);
        layers.add("humanoid_leggings", leggings);

        JsonObject root = new JsonObject();
        root.add("layers", layers);

        Path path = output.getOutputFolder()
                .resolve("assets/" + HarambeCore.MOD_ID + "/equipment/" + name + ".json");
        return DataProvider.saveStable(cache, root, path);
    }

    @Override public String getName() { return "Equipment Assets - " + HarambeCore.MOD_ID; }
}
