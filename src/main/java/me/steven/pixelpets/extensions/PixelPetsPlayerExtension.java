package me.steven.pixelpets.extensions;

import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.items.PetData;
import me.steven.pixelpets.pets.PixelPet;

import java.util.Map;

public interface PixelPetsPlayerExtension {
    Map<PixelPet, Map<Ability, PetData>> getTickingAbilities();
}