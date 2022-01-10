package me.steven.pixelpets.extensions;

import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.abilities.AbilityAction;
import me.steven.pixelpets.pets.PetData;
import me.steven.pixelpets.pets.PixelPet;

import java.util.Map;

public interface PixelPetsPlayerExtension {
    Map<PixelPet, Map<AbilityAction, PetData>> getTickingAbilities();
}