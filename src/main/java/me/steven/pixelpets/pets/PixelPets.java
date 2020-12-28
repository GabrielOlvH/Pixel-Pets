package me.steven.pixelpets.pets;

import com.google.common.collect.HashBiMap;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import net.minecraft.util.Identifier;
public class PixelPets {

    public static final HashBiMap<Identifier, PixelPet> REGISTRY = HashBiMap.create();

    private PixelPets() {}

    //public static final PixelPet PIG = new PixelPet(new Identifier(PixelPetsMod.MOD_ID, "pig"), 0xde78be, Abilities.SATURATION, Abilities.PREY, Abilities.INSTA_REGEN, Abilities.MORE_HEARTS);
}