package me.steven.pixelpets.pets;

import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.abilities.Abilities;
import net.minecraft.util.Identifier;
public class PixelPets {

    private PixelPets() {}

    public static final PixelPet PIG = new PixelPet(new Identifier(PixelPetsMod.MOD_ID, "pig"), 0xde78be, Abilities.SATURATION, Abilities.PREY, Abilities.INSTA_REGEN, Abilities.MORE_HEARTS);
}