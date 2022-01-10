package me.steven.pixelpets.pets;

import com.google.common.collect.HashBiMap;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class PixelPets {

    public static final HashBiMap<Identifier, PixelPet> REGISTRY = HashBiMap.create();
    public static final Set<Identifier> MODEL_IDENTIFIERS = new HashSet<>();

    private PixelPets() {}
}