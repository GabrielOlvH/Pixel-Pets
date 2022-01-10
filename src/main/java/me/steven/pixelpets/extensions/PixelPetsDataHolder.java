package me.steven.pixelpets.extensions;

import me.steven.pixelpets.pets.PetData;
import org.jetbrains.annotations.Nullable;

public interface PixelPetsDataHolder {

    @Nullable
    PetData getPetData();

    void setPetData(PetData data);
}
