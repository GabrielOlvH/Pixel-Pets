package me.steven.pixelpets.json.pets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.pets.PixelPet;
import me.steven.pixelpets.pets.PixelPets;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PetResourceReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger("PetResourceReloadListener");

    @Override
    public Identifier getFabricId() {
        return new Identifier(PixelPetsMod.MOD_ID, "pet_resource_reloader");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return Collections.singletonList(new Identifier(PixelPetsMod.MOD_ID, "ability_resource_reloader"));
    }

    @Override
    public void apply(ResourceManager manager) {
        Collection<Identifier> pets = manager.findResources("pets", (r) -> r.endsWith(".json") || r.endsWith(".json5"));
        for (Identifier fileId : pets) {
            try (
                    InputStream is = manager.getResource(fileId).getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is))
            ) {
                JsonObject result = new JsonParser().parse(reader).getAsJsonObject();
                Identifier id = new Identifier(result.get("id").getAsString());
                int cooldownColorDisplay = Integer.decode(result.get("color").getAsString());
                JsonArray abilitiesArray = result.getAsJsonArray("abilities");
                List<Identifier> abilities = new ArrayList<>(abilitiesArray.size());
                for (int i = 0; i < abilitiesArray.size(); i++) {
                    abilities.add(null);
                }
                abilitiesArray.forEach((element) -> {
                    JsonObject obj = element.getAsJsonObject();
                    int pos = obj.get("pos").getAsInt();
                    String abilityId = obj.get("id").getAsString();
                    abilities.set(pos, new Identifier(abilityId));
                });
                List<PixelPet.Variant> variants = new ArrayList<>();
                if (!result.has("variants")) {
                    variants.add(new PixelPet.Variant(0, id, null));
                } else if (result.get("variants").isJsonPrimitive()) {
                    int totalVariants = result.get("variants").getAsInt();
                    for (int i = 0; i < totalVariants; i++) {
                        variants.add(new PixelPet.Variant(i, id, null));
                    }
                } else if (result.get("variants").isJsonArray()) {
                    result.get("variants").getAsJsonArray().forEach(element -> {
                        JsonObject obj = element.getAsJsonObject();
                        String translationKey = JsonHelper.getString(obj, "translationKey", null);
                        int index = obj.get("index").getAsInt();
                        variants.add(new PixelPet.Variant(index, id, translationKey));
                    });
                }
                PixelPets.REGISTRY.put(id, new PixelPet(id, cooldownColorDisplay, variants, abilities.stream().map(Abilities.REGISTRY::get).toArray(Ability[]::new)));
            } catch (IOException e) {
                LOGGER.error("Unable to load pet from '" + fileId + "'.", e);
            }
        }
        LOGGER.info("Loaded " + PixelPets.REGISTRY.size() + " pets!");
    }
}