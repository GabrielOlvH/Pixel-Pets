package me.steven.pixelpets.json.abilities;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.Optional;
import java.util.UUID;

public class EntityAttributeParser {
    public static Multimap<EntityAttribute, EntityAttributeModifier> parse(JsonElement element) {
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        if (element == null) return builder.build();

        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(obj -> {
                Pair<EntityAttribute, EntityAttributeModifier> pair = parse(obj.getAsJsonObject());
                builder.put(pair.getLeft(), pair.getRight());
            });
        } else if (element.isJsonObject()) {
            Pair<EntityAttribute, EntityAttributeModifier> pair = parse(element.getAsJsonObject());
            builder.put(pair.getLeft(), pair.getRight());
        }

        return builder.build();
    }

    private static Pair<EntityAttribute, EntityAttributeModifier> parse(JsonObject object) {
        Identifier id = new Identifier(object.get("id").getAsString());
        Optional<EntityAttribute> attributeOptional = Registry.ATTRIBUTE.getOrEmpty(id);
        if (!attributeOptional.isPresent())
            throw new NullPointerException("Expected entity attribute id but received unknown string '" + id + "' instead.");
        UUID uuid = UUID.fromString(JsonHelper.getString(object, "uuid", UUID.randomUUID().toString()));
        String name = object.get("name").getAsString();
        EntityAttribute att = attributeOptional.get();
        int value = object.get("value").getAsInt();
        EntityAttributeModifier.Operation operation = EntityAttributeModifier.Operation.valueOf(object.get("operation").getAsString());
        EntityAttributeModifier modifier = new EntityAttributeModifier(uuid, name, value, operation);
        return new Pair<>(att, modifier);
    }
}
