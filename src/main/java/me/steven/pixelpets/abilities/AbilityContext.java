package me.steven.pixelpets.abilities;

import me.steven.pixelpets.pets.PetData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@FunctionalInterface
public interface AbilityContext {
    boolean test(PetData petData, World world, LivingEntity entity);
}
