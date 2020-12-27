package me.steven.pixelpets.pets;

import me.steven.pixelpets.abilities.Ability;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PixelPet {
    private final Identifier id;
    private final String translationKey;
    private final Ability standard;
    private final Ability[] abilities;
    private final int cooldownDisplayColor;

    public PixelPet(Identifier id, int cooldownDisplayColor, Ability... abilities) {
        this.id = id;
        this.cooldownDisplayColor = cooldownDisplayColor;
        this.translationKey = "pet." + id.getNamespace() + "." + id.getPath();
        this.standard = abilities[0];
        this.abilities = abilities;
    }

    public Identifier getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public Ability getStandard() {
        return standard;
    }

    public Ability[] getAbilities() {
        return abilities;
    }

    public int getCooldownDisplayColor() {
        return cooldownDisplayColor;
    }
}