package me.steven.pixelpets.items;

import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.pets.PetData;
import me.steven.pixelpets.pets.PixelPets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PixelPetEggItem extends Item {
    public PixelPetEggItem() {
        super(new Item.Settings());
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        NbtCompound tag = stack.getOrCreateNbt();
        if (!tag.contains("hatching")) {
            tag.putInt("hatching", 500);
        } else {
            int hatching = tag.getInt("hatching");
            tag.putInt("hatching", hatching - 1);
            if (hatching <= 0) {
                ItemStack petStack = new ItemStack(PixelPetsMod.PET_ITEM);
                PetData data;
                if (tag.contains("PetData")) {
                    data = PetData.fromTag(stack);
                } else {
                    Identifier[] pets = PixelPets.REGISTRY.keySet().toArray(Identifier[]::new);
                    Identifier pet = pets[world.random.nextInt(pets.length)];
                    data = new PetData(pet);
                }
                petStack.getOrCreateNbt().put("PetData", data.toTag());
                ((PlayerEntity) entity).getInventory().setStack(slot, petStack);
            }
        }
    }
}
