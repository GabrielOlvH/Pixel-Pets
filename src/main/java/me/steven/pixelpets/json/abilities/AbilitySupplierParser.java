package me.steven.pixelpets.json.abilities;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Function;
import me.steven.pixelpets.utils.AbilitySupplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Optional;
import java.util.function.Supplier;

public class AbilitySupplierParser {
    public static Optional<AbilitySupplier> parse(JsonObject object) {
        if (object == null) return Optional.empty();
        Optional<Function<Entity, Boolean>> condition = object.has("condition") ? AbilityParser.parseEntityCondition(object.getAsJsonObject("condition")) : Optional.empty();
        if (object.has("effect")) {
            Optional<StatusEffectInstance> statusOptional = AbilityParser.parseStatusEffect(object.getAsJsonObject("effect"));
            if (statusOptional.isPresent()) {
                return Optional.of((stack, world, entity) -> {
                    if (!condition.isPresent() || condition.get().apply(entity)) {
                        entity.addStatusEffect(statusOptional.get());
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
        return Optional.empty();
    }
}
