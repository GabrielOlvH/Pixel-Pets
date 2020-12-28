package me.steven.pixelpets.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@FunctionalInterface
public interface AbilitySupplier {
    boolean test(ItemStack stack, World world, LivingEntity entity);
}
