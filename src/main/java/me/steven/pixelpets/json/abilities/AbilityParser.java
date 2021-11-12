package me.steven.pixelpets.json.abilities;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.Function;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class AbilityParser {
    public static Optional<Supplier<StatusEffectInstance>> parseStatusEffect(JsonObject object) {
        String id = object.get("id").getAsString();
        Optional<StatusEffect> optional = Registry.STATUS_EFFECT.getOrEmpty(new Identifier(id));
        if (!optional.isPresent())
            throw new NullPointerException("Expected status effect id but received unknown string '" + id + "'.");
        StatusEffect statusEffect = optional.get();
        Optional<Supplier<Integer>> durationOptional = parseIntProvider(object.get("duration"));
        if (!durationOptional.isPresent()) throw new NullPointerException("No duration!");
        int duration = durationOptional.get().get();

        Optional<Supplier<Integer>> amplifierOptional = parseIntProvider(object.get("amplifier"));
        if (!amplifierOptional.isPresent()) throw new NullPointerException("No amplifier!");
        int amplifier = amplifierOptional.get().get();
        return Optional.of(() -> new StatusEffectInstance(statusEffect, duration, amplifier));
    }

    public static Optional<Function<Entity, Boolean>> parseEntityCondition(JsonObject object) {
        if (object.has("health")) {
            Optional<Function<Float, Boolean>> condition = parseFloatCondition(object.get("health"));
            if (condition.isPresent()) {
                return Optional.of((player) -> condition.get().apply(((LivingEntity) player).getHealth()));
            }
        }

        if (object.has("hunger")) {
            Optional<Function<Integer, Boolean>> condition = parseIntCondition(object.get("hunger"));
            if (condition.isPresent()) {
                return Optional.of((player) -> condition.get().apply(((PlayerEntity) player).getHungerManager().getFoodLevel()));
            }
        }

        if (object.has("saturation")) {
            Optional<Function<Float, Boolean>> condition = parseFloatCondition(object.get("saturation"));
            if (condition.isPresent()) {
                return Optional.of((player) -> condition.get().apply(((PlayerEntity) player).getHungerManager().getSaturationLevel()));
            }
        }

        if (object.has("isNearHostiles")) {
            int inRadius = object.get("isNearHostiles").getAsInt();
            return Optional.of((obj) -> {
                LivingEntity player = (LivingEntity) obj;
                return !player.world.getEntitiesByClass(HostileEntity.class, new Box(player.getBlockPos()).expand(inRadius), (e) -> true).isEmpty();
            });
        }

        if (object.has("isUnderwater")) {
            boolean value = object.get("isUnderwater").getAsBoolean();
            return Optional.of((obj) -> {
                LivingEntity player = (LivingEntity) obj;
                return player.isSubmergedInWater() == value;
            });
        }

        if (object.has("isFallFlying")) {
            boolean value = object.get("isFallFlying").getAsBoolean();
            return Optional.of((obj) -> {
                LivingEntity player = (LivingEntity) obj;
                return player.isFallFlying() == value;
            });
        }

        if (object.has("fireTicks")) {
            Optional<Function<Integer, Boolean>> fireTicks = parseIntCondition(object.get("fireTicks"));
            return Optional.of((obj) -> {
                LivingEntity player = (LivingEntity) obj;
                return fireTicks.isPresent() && fireTicks.get().apply(player.getFireTicks());
            });
        }

        if (object.has("hasStatusEffect")) {
            String effectId = object.get("hasStatusEffect").getAsString();
            Optional<StatusEffect> effect = Registry.STATUS_EFFECT.getOrEmpty(new Identifier(effectId));
            return Optional.of((obj) -> {
                LivingEntity player = (LivingEntity) obj;
                return effect.isPresent() && player.hasStatusEffect(effect.get());
            });
        }

        if (object.has("nbt")) {
            JsonObject nbt = object.get("nbt").getAsJsonObject();
            return Optional.of((obj) -> {
                LivingEntity player = (LivingEntity) obj;
                NbtCompound compoundTag = player.writeNbt(new NbtCompound());
                return nbt.entrySet().stream().allMatch(entry -> {
                    if (!compoundTag.contains(entry.getKey())) return false;
                    JsonElement element = entry.getValue();

                    Optional<Function<Integer, Boolean>> intCondition = parseIntCondition(element);
                    if (intCondition.isPresent())
                        return intCondition.get().apply(compoundTag.getInt(entry.getKey()));

                    Optional<Function<String, Boolean>> stringCondition = parseStringCondition(element);
                    if (stringCondition.isPresent())
                        return stringCondition.get().apply(compoundTag.getString(entry.getKey()));

                    Optional<Supplier<Boolean>> booleanCondition = parseBooleanCondition(element);
                    if (booleanCondition.isPresent())
                        return booleanCondition.get().get() == compoundTag.getBoolean(entry.getKey());

                    throw new UnsupportedOperationException("Unsupported condition for '" + entry.getKey() + "': " + entry.getValue());
                });
            });
        }

        return Optional.empty();
    }

    public static Optional<Supplier<Integer>> parseIntProvider(JsonElement element) {
        if (element == null) return Optional.empty();
        else if (element.isJsonPrimitive()) return Optional.of(element::getAsInt);
        else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            int min = JsonHelper.getInt(object, "min", Integer.MIN_VALUE);
            int max = JsonHelper.getInt(object, "max", Integer.MAX_VALUE);
            return Optional.of(() -> ThreadLocalRandom.current().nextInt(max - min) + min);
        }
        return Optional.empty();
    }

    public static Optional<Function<EntityType<?>, Boolean>> parseEntityType(JsonElement object) {
        if (object == null) return Optional.empty();
        else if (object.isJsonArray()) {
            Set<Identifier> types = new HashSet<>();
            object.getAsJsonArray().forEach((e) -> {
                Identifier id = new Identifier(e.getAsString());
                Optional<EntityType<?>> optional = Registry.ENTITY_TYPE.getOrEmpty(id);
                if (!optional.isPresent()) throw new NullPointerException("Expected entity type but received unknown string '" + e.getAsString() + "'.");
                types.add(id);
            });
            return Optional.of((t) -> types.contains(Registry.ENTITY_TYPE.getId((EntityType<?>) t)));
        } else {
            Identifier id = new Identifier(object.getAsString());
            Optional<EntityType<?>> optional = Registry.ENTITY_TYPE.getOrEmpty(id);
            if (!optional.isPresent()) throw new NullPointerException("Expected entity type but received unknown string '" + object.getAsString() + "'.");
            return Optional.of((t) -> t == optional.get());
        }
    }

    public static Optional<Supplier<ItemStack>> parseStackProvider(JsonObject object) {
        Identifier id = new Identifier(object.get("id").getAsString());
        Item item = Registry.ITEM.get(id);
        Optional<Supplier<Integer>> countProvider = parseIntProvider(object.get("count"));
        int count = countProvider.orElse(() -> 1).get();
        NbtCompound nbt = new NbtCompound();
        if (object.has("nbt")) {
            object.getAsJsonObject("nbt").entrySet().forEach(e -> {
                JsonPrimitive value = e.getValue().getAsJsonPrimitive();
                if (value.isNumber())
                    nbt.putInt(e.getKey(), value.getAsInt());
                else if (value.isString())
                    nbt.putString(e.getKey(), value.getAsString());
                else if (value.isBoolean())
                    nbt.putBoolean(e.getKey(), value.getAsBoolean());
            });
        }
        return Optional.of(() -> {
            ItemStack itemStack = new ItemStack(item, count);
            if (!nbt.isEmpty()) itemStack.setNbt(nbt);
            return itemStack;
        });
    }

    public static Optional<Function<Integer, Boolean>> parseIntCondition(JsonElement element) {
        if (element.isJsonPrimitive()) return Optional.of((i) -> (int) i == element.getAsInt());
        else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            int min = JsonHelper.getInt(object, "min", Integer.MIN_VALUE);
            int max = JsonHelper.getInt(object, "max", Integer.MAX_VALUE);
            return Optional.of((i) -> (int) i >= min && (int) i <= max);
        } else return Optional.empty();

    }

    public static Optional<Function<Float, Boolean>> parseFloatCondition(JsonElement element) {
        if (element.isJsonPrimitive()) return Optional.of((i) -> (float) i == element.getAsInt());
        else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            float min = JsonHelper.getFloat(object, "min", Integer.MIN_VALUE);
            float max = JsonHelper.getFloat(object, "max", Integer.MAX_VALUE);
            return Optional.of((i) -> (float) i >= min && (float) i <= max);
        } else return Optional.empty();
    }

    public static Optional<Supplier<Boolean>> parseBooleanCondition(JsonElement element) {
        return Optional.of(element::getAsBoolean);
    }

    public static Optional<Function<String, Boolean>> parseStringCondition(JsonElement element) {
        if (element.isJsonPrimitive()) return Optional.of((i) -> element.getAsString().equals(i));
        else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            String value = object.get("value").getAsString();
            if (object.has("contains"))
                return Optional.of((i) -> ((String) i).contains(value));
             else if (object.has("startsWith"))
                return Optional.of((i) -> ((String) i).startsWith(value));
            else if (object.has("endsWith"))
                return Optional.of((i) -> ((String) i).endsWith(value));
            else if (object.has("equals"))
                return Optional.of((i) -> i.equals(value));
            else if (object.has("equalsIgnoreCase"))
                return Optional.of((i) -> ((String) i).equalsIgnoreCase(value));
        }
        return Optional.empty();
    }
}
