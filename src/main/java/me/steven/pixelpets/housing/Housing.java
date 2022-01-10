package me.steven.pixelpets.housing;

import com.google.common.collect.HashBiMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public record Housing(
        Identifier id,
        int capacity,
        int color
) {

    public static final HashBiMap<Identifier, Housing> REGISTRY = HashBiMap.create();

}