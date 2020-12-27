package me.steven.pixelpets.utils;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@FunctionalInterface
public interface InventoryTick {
    boolean inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected);
}
