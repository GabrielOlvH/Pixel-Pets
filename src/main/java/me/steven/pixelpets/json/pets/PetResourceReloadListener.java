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
    public void reload(ResourceManager manager) {
        Collection<Identifier> pets = manager.findResources("pets", (r) -> r.toString().endsWith(".json") || r.toString().endsWith(".json5")).keySet();
        for (Identifier fileId : pets) {
            try (
                    InputStream is = manager.getResource(fileId).get().getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is))
            ) {
                JsonObject result = new JsonParser().parse(reader).getAsJsonObject();
                Identifier id = new Identifier(result.get("id").getAsString());
                Identifier housingId = result.has("housing") ? new Identifier(result.get("housing").getAsString()) : null;
                Identifier eggGroupId = result.has("eggGroup") ? new Identifier(result.get("eggGroup").getAsString()) : null;
                int color = Integer.decode(result.get("color").getAsString());
                JsonObject abilitiesArray = result.getAsJsonObject("abilities");
                List<Identifier> abilities = new ArrayList<>(abilitiesArray.size());
                for (int i = 0; i < abilitiesArray.size(); i++) {
                    abilities.add(null);
                }
                abilitiesArray.entrySet().forEach((entry) -> {
                    String key = entry.getKey();
                    int pos = Integer.parseInt(key);
                    String abilityId = entry.getValue().getAsString();
                    if (!Abilities.REGISTRY.containsKey(new Identifier(abilityId))) throw new IllegalArgumentException("Expected ability id but received unknown string '" + abilityId + "'");
                    abilities.set(pos, new Identifier(abilityId));
                });
                List<PixelPet.Variant> variants = new ArrayList<>();
                if (!result.has("variants")) {
                    variants.add(new PixelPet.Variant(0, id, color, null));
                } else if (result.get("variants").isJsonPrimitive()) {
                    int totalVariants = result.get("variants").getAsInt();
                    for (int i = 0; i < totalVariants; i++) {
                        variants.add(new PixelPet.Variant(i, id, color,null));
                    }
                } else if (result.get("variants").isJsonArray()) {
                    result.get("variants").getAsJsonArray().forEach(element -> {
                        JsonObject obj = element.getAsJsonObject();
                        String translationKey = JsonHelper.getString(obj, "translationKey", null);
                        int variantColor = Integer.decode(JsonHelper.getString(obj, "color", result.get("color").getAsString()));
                        int index = obj.get("index").getAsInt();
                        variants.add(new PixelPet.Variant(index, id, variantColor, translationKey));
                    });
                }
                PixelPets.REGISTRY.put(id, new PixelPet(id, eggGroupId, housingId, color, variants, abilities.stream().map(Abilities.REGISTRY::get).toArray(Ability[]::new)));
            } catch (IOException e) {
                LOGGER.error("Unable to load pet from '" + fileId + "'.", e);
            }
        }
        LOGGER.info("Loaded " + PixelPets.REGISTRY.size() + " pets!");
    }
}