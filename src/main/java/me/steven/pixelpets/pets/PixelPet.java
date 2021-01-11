package me.steven.pixelpets.pets;

import me.steven.pixelpets.abilities.Ability;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PixelPet {
    private final Identifier id;
    private final String translationKey;
    private final Ability[] abilities;
    private final int cooldownDisplayColor;
    private final List<Variant> variants;

    public PixelPet(Identifier id, int cooldownDisplayColor, List<Variant> variants, Ability... abilities) {
        this.id = id;
        this.cooldownDisplayColor = cooldownDisplayColor;
        this.translationKey = "pet." + id.getNamespace() + "." + id.getPath();
        this.variants = variants.stream().sorted(Comparator.comparingInt((a) -> a.id)).collect(Collectors.toList());
        this.abilities = abilities;
    }

    public Identifier getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public Ability[] getAbilities() {
        return abilities;
    }

    public int getCooldownDisplayColor() {
        return cooldownDisplayColor;
    }

    public List<Variant> getVariants() {
        return variants;
    }

    public String getTranslationKey(int variant) {
        Variant v = variants.get(variant);
        return v.translationKey == null ? translationKey : v.translationKey;
    }

    public static class Variant {
        private final int id;
        private final Identifier parentId;
        @Nullable
        private final String translationKey;

        public Variant(int id, Identifier parentId, @Nullable String translationKey) {
            this.id = id;
            this.parentId = parentId;
            this.translationKey = translationKey;
        }

        public int getId() {
            return id;
        }

        public Identifier getParentId() {
            return parentId;
        }

        @Nullable
        public String getTranslationKey() {
            return translationKey;
        }
    }
}