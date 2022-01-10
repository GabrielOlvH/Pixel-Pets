package me.steven.pixelpets.pets;

import me.steven.pixelpets.abilities.Ability;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class PixelPet {
    private final Identifier id;
    @Nullable
    private final Identifier housingId;
    private final String translationKey;
    private final Ability[] abilities;
    private final int color;
    private final List<Variant> variants;

    public PixelPet(Identifier id, @Nullable Identifier housing, int color, List<Variant> variants, Ability... abilities) {
        this.id = id;
        this.housingId = housing;
        this.color = color;
        this.translationKey = "pet." + id.getNamespace() + "." + id.getPath();
        this.variants = variants.stream().sorted(Comparator.comparingInt((a) -> a.id)).collect(Collectors.toList());
        this.abilities = abilities;
    }

    public Identifier getId() {
        return id;
    }


    @Nullable
    public Identifier getHousingId() {
        return housingId;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public Ability[] getAbilities() {
        return abilities;
    }

    public int getColor() {
        return color;
    }

    public int getColor(int variant) {
        Variant v = variants.get(variant);
        return v.color;
    }

    public List<Variant> getVariants() {
        return variants;
    }

    public Variant getRandomVariant() {
        return variants.get(ThreadLocalRandom.current().nextInt(variants.size()));
    }

    public String getTranslationKey(int variant) {
        Variant v = variants.get(variant);
        return v.translationKey == null ? translationKey : v.translationKey;
    }

    public record Variant(
            int id,
            Identifier parentId,
            int color,
            @Nullable String translationKey
    ) {}
}