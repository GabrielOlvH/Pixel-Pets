package me.steven.pixelpets.player;

import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.items.PetData;
import me.steven.pixelpets.pets.PixelPet;

import java.util.Map;
import java.util.Set;

public interface PixelPetsPlayerExtension {
    Map<PixelPet, Set<Ability>> getInventoryPets();
}