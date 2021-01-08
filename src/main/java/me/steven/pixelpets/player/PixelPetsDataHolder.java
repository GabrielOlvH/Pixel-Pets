package me.steven.pixelpets.player;

import me.steven.pixelpets.items.PetData;
import org.jetbrains.annotations.Nullable;

public interface PixelPetsDataHolder {

    @Nullable
    PetData getPetData();

    void setPetData(PetData data);
}
