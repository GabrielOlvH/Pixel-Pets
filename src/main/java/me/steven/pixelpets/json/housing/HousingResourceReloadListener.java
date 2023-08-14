package me.steven.pixelpets.json.housing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.housing.Housing;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

public class HousingResourceReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger("HousingResourceReloadListener");

    @Override
    public Identifier getFabricId() {
        return new Identifier(PixelPetsMod.MOD_ID, "housing_resource_reloader");
    }

    @Override
    public void reload(ResourceManager manager) {
        Collection<Identifier> pets = manager.findResources("housing", (r) -> r.toString().endsWith(".json") || r.toString().endsWith(".json5")).keySet();
        for (Identifier fileId : pets) {
            try (
                    InputStream is = manager.getResource(fileId).get().getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is))
            ) {
                JsonObject result = new JsonParser().parse(reader).getAsJsonObject();
                Identifier id = new Identifier(result.get("id").getAsString());
                int capacity = result.get("capacity").getAsInt();
                int color = Integer.decode(result.get("color").getAsString());

                Housing.REGISTRY.put(id, new Housing(id, capacity, color));
            } catch (IOException e) {
                LOGGER.error("Unable to load housing from '" + fileId + "'.", e);
            }
        }
        LOGGER.info("Loaded " + Housing.REGISTRY.size() + " housings!");
    }
}