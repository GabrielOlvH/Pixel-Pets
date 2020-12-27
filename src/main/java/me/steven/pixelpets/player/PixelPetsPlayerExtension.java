package me.steven.pixelpets.player;

import me.steven.pixelpets.items.PetData;
import me.steven.pixelpets.pets.PixelPet;

import java.util.Map;

public interface PixelPetsPlayerExtension {
    Map<PixelPet, PetData> getInventoryPets();
}