package me.steven.pixelpets.json.abilities;

import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.Function;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.abilities.AbilityRarity;
import me.steven.pixelpets.utils.AbilitySupplier;
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
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

public class AbilityResourceReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger("AbilityResourceReloadListener");

    @Override
    public Identifier getFabricId() {
        return new Identifier(PixelPetsMod.MOD_ID, "ability_resource_reloader");
    }

    @Override
    public void reload(ResourceManager manager) {
        Collection<Identifier> abilities = manager.findResources("abilities", (r) -> r.endsWith(".json") || r.endsWith(".json5"));
        for (Identifier fileId : abilities) {
            try (
                    InputStream is = manager.getResource(fileId).getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is))
            ) {
                JsonObject result = new JsonParser().parse(reader).getAsJsonObject();
                Identifier id = new Identifier(result.get("id").getAsString());
                int cooldown = result.get("cooldown").getAsInt();
                String rarityString = result.get("rarity").getAsString();
                AbilityRarity rarity = AbilityRarity.valueOf(rarityString.toUpperCase(Locale.ROOT));
                Optional<AbilitySupplier> tick = AbilitySupplierParser.parse(result.has("tick") ? result.get("tick").getAsJsonObject() : null);
                Optional<AbilitySupplier> interact = AbilitySupplierParser.parse(result.has("interact") ? result.get("interact").getAsJsonObject() : null);
                Multimap<EntityAttribute, EntityAttributeModifier> attributes = EntityAttributeParser.parse(result.get("attributes"));
                Optional<Function<EntityType<?>, Boolean>> repels = AbilityParser.parseEntityType(result.get("repels"));
                Optional<Supplier<StatusEffectInstance>> passiveEffect = result.has("passiveEffect") ? AbilityParser.parseStatusEffect(result.getAsJsonObject("passiveEffect")) : Optional.empty();
                Ability ability = new Ability() {
                    @Override
                    public Identifier getId() {
                        return id;
                    }

                    @Override
                    public AbilityRarity getRarity() {
                        return rarity;
                    }

                    @Override
                    public boolean onInteract(ItemStack stack, World world, LivingEntity entity) {
                        return interact.isPresent() && interact.get().test(stack, world, entity);
                    }

                    @Override
                    public boolean inventoryTick(ItemStack stack, World world, LivingEntity entity) {
                        return tick.isPresent() && tick.get().test(stack, world, entity);
                    }

                    @Override
                    @Nullable
                    public StatusEffectInstance getPassiveEffect(World world, LivingEntity entity) {
                        return passiveEffect.map(Supplier::get).orElse(null);
                    }

                    @Override
                    public int getCooldown() {
                        return cooldown;
                    }

                    @Override
                    public @Nullable Multimap<EntityAttribute, EntityAttributeModifier> getEntityAttributeModifiers() {
                        return attributes;
                    }

                    @Override
                    public boolean repels(EntityType<?> type) {
                        return repels.isPresent() && repels.get().apply(type);
                    }
                };
                Abilities.REGISTRY.put(id, ability);
            } catch (IOException e) {
                LOGGER.error("Unable to load ability from '" + fileId + "'.", e);
            }
        }
        LOGGER.info("Loaded " + Abilities.REGISTRY.size() + " abilities!");
    }
}
