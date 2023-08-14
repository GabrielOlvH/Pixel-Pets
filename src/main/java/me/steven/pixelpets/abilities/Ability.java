package me.steven.pixelpets.abilities;

import net.minecraft.util.Identifier;

public record Ability(Identifier id, AbilityAction action) {
    public String getTranslationKey() {
        return "ability." + id().getNamespace() + "." + id().getPath();
    }
}
