package me.steven.pixelpets.pets;

import com.google.common.collect.HashBiMap;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class PixelPets {

    public static final HashBiMap<Identifier, PixelPet> REGISTRY = HashBiMap.create();
    public static final Set<Identifier> MODELS_TO_BAKE = new HashSet<>();

    private PixelPets() {}

    //public static final PixelPet PIG = new PixelPet(new Identifier(PixelPetsMod.MOD_ID, "pig"), 0xde78be, Abilities.SATURATION, Abilities.PREY, Abilities.INSTA_REGEN, Abilities.MORE_HEARTS);
}