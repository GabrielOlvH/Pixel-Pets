package me.steven.pixelpets.json.abilities;

import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.Function;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.abilities.AbilityAction;
import me.steven.pixelpets.abilities.AbilityContext;
import me.steven.pixelpets.pets.PetData;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Supplier;

public class AbilityResourceReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger("AbilityResourceReloadListener");

    @Override
    public Identifier getFabricId() {
        return new Identifier(PixelPetsMod.MOD_ID, "ability_resource_reloader");
    }

    @Override
    public void reload(ResourceManager manager) {
        Collection<Identifier> pets = manager.findResources("abilities", (r) -> r.toString().endsWith(".json") || r.toString().endsWith(".json5")).keySet();
        for (Identifier fileId : pets) {
            try (
                    InputStream is = manager.getResource(fileId).get().getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is))
            ) {
                JsonObject result = new JsonParser().parse(reader).getAsJsonObject();
                Identifier id = new Identifier(result.get("id").getAsString());
                JsonObject action = result.getAsJsonObject("action");
                int cooldown = action.has("cooldown") ? action.get("cooldown").getAsInt() : 0;
                Optional<AbilityContext> tick = AbilitySupplierParser.parse(action.has("tick") ? action.get("tick").getAsJsonObject() : null);
                Optional<AbilityContext> interact = AbilitySupplierParser.parse(action.has("interact") ? action.get("interact").getAsJsonObject() : null);
                Optional<AbilityContext> onDamaged = AbilitySupplierParser.parse(action.has("onDamage") ? action.get("onDamage").getAsJsonObject() : null);
                Multimap<EntityAttribute, EntityAttributeModifier> attributes = EntityAttributeParser.parse(action.get("attributes"));
                Optional<Function<EntityType<?>, Boolean>> repels = AbilityParser.parseEntityType(action.get("repels"));
                Optional<Supplier<StatusEffectInstance>> passiveEffect = action.has("passiveEffect") ? AbilityParser.parseStatusEffect(action.getAsJsonObject("passiveEffect")) : Optional.empty();
                AbilityAction a = new AbilityAction() {

                    @Override
                    public int getCooldown() {
                        return cooldown;
                    }

                    @Override
                    public boolean onInteract(PetData petData, World world, LivingEntity entity) {
                        return interact.isPresent() && interact.get().test(petData, world, entity);
                    }

                    @Override
                    public boolean inventoryTick(PetData petData, World world, LivingEntity entity) {
                        return tick.isPresent() && tick.get().test(petData, world, entity);
                    }

                    @Override
                    public @Nullable StatusEffectInstance getPassiveEffect(World world, LivingEntity entity) {
                        return passiveEffect.map(Supplier::get).orElse(null);
                    }

                    @Override
                    public boolean onDamaged(PetData petData, World world, LivingEntity entity) {
                        return onDamaged.isPresent() && onDamaged.get().test(petData, world, entity);
                    }

                    @Override
                    public @NotNull Multimap<EntityAttribute, EntityAttributeModifier> getEntityAttributeModifiers() {
                        return attributes;
                    }

                    @Override
                    public boolean repels(EntityType<?> type) {
                        return repels.isPresent() && repels.get().apply(type);
                    }

                    ;
                };
                Ability ability = new Ability(id, a);

                Abilities.REGISTRY.put(id, ability);
            } catch (Exception e) {
                LOGGER.error("Unable to load ability from '" + fileId + "'.", e);
            }
        }
        LOGGER.info("Loaded " + Abilities.REGISTRY.size() + " abilities!");
    }
}
