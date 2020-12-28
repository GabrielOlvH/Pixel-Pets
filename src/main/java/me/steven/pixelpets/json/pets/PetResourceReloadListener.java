package me.steven.pixelpets.json.pets;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.abilities.AbilityRarity;
import me.steven.pixelpets.json.abilities.AbilitySupplierParser;
import me.steven.pixelpets.json.abilities.EntityAttributeParser;
import me.steven.pixelpets.pets.PixelPet;
import me.steven.pixelpets.pets.PixelPets;
import me.steven.pixelpets.utils.AbilitySupplier;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

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
                PixelPets.REGISTRY.put(id, new PixelPet(id, cooldownColorDisplay, abilities.stream().map(Abilities.REGISTRY::get).toArray(Ability[]::new)));
            } catch (IOException e) {
                LOGGER.error("Unable to load pet from '" + fileId + "'.", e);
            }
        }
        LOGGER.info("Loaded " + PixelPets.REGISTRY.size() + " pets!");
    }
}