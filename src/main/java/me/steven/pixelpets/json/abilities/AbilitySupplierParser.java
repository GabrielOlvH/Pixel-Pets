package me.steven.pixelpets.json.abilities;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Function;
import me.steven.pixelpets.extensions.PixelPetsDataHolder;
import me.steven.pixelpets.items.PetData;
import me.steven.pixelpets.utils.AbilitySupplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;

import java.util.Optional;
import java.util.function.Supplier;

public class AbilitySupplierParser {
    public static Optional<AbilitySupplier> parse(JsonObject object) {
        if (object == null) return Optional.empty();
        Optional<Function<Entity, Boolean>> condition = object.has("condition") ? AbilityParser.parseEntityCondition(object.getAsJsonObject("condition")) : Optional.empty();
        if (object.has("effect")) {
            Optional<Supplier<StatusEffectInstance>> statusOptional = AbilityParser.parseStatusEffect(object.getAsJsonObject("effect"));
            if (statusOptional.isPresent()) {
                return Optional.of((stack, world, entity) -> {
                    if (!condition.isPresent() || condition.get().apply(entity)) {
                        StatusEffectInstance effect = statusOptional.get().get();
                        PixelPetsDataHolder ext = (PixelPetsDataHolder) effect;
                        ext.setPetData(PetData.fromTag(stack));
                        entity.addStatusEffect(effect);
                        return true;
                    }
                    return false;
                });
            }
        }
        if (object.has("heal")) {
            Optional<Supplier<Integer>> heal = AbilityParser.parseIntProvider(object.get("heal"));
            if (heal.isPresent()) {
                return Optional.of((stack, world, entity) -> {
                    if (!condition.isPresent() || condition.get().apply(entity)) {
                        entity.heal(heal.get().get());
                        return true;
                    }
                    return false;
                });
            }
        }
        if (object.has("give")) {
            Optional<Supplier<ItemStack>> give = AbilityParser.parseStackProvider(object.get("give").getAsJsonObject());
            if (give.isPresent()) {
                return Optional.of((stack, world, entity) -> {
                    if ((!condition.isPresent() || condition.get().apply(entity))) {
                        ItemScatterer.spawn(world, entity.getX(), entity.getY(), entity.getZ(), give.get().get());
                        return true;
                    }
                    return false;
                });
            }
        }

        if (object.has("air")) {
            Optional<Supplier<Integer>> air = AbilityParser.parseIntProvider(object.get("air"));
            if (air.isPresent()) {
                return Optional.of((stack, world, entity) -> {
                    if ((!condition.isPresent() || condition.get().apply(entity))) {
                        entity.setAir(Math.min(entity.getAir() + air.get().get(), entity.getMaxAir()));
                        return true;
                    }
                    return false;
                });
            }
        }
        return Optional.of((stack, world, entity) -> !condition.isPresent() || condition.get().apply(entity));
    }
}
