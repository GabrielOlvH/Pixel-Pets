package me.steven.pixelpets.items;

import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.abilities.AbilityRarity;
import me.steven.pixelpets.pets.PixelPets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;

public class PixelPetEggItem extends Item {
    public PixelPetEggItem() {
        super(new Item.Settings());
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        CompoundTag tag = stack.getOrCreateTag();
        boolean isBreed = tag.contains("isBreed") && tag.getBoolean("isBreed");
        if (!tag.contains("hatching")) {
            tag.putInt("hatching", 500);
        } else {
            int hatching = tag.getInt("hatching");
            tag.putInt("hatching", hatching - 1);
            if (hatching <= 0) {
                ItemStack petStack = new ItemStack(PixelPetsMod.PET_ITEM);
                Identifier id;
                if (tag.contains("PetId"))
                    id = new Identifier(tag.getString("PetId"));
                else {
                    ArrayList<Identifier> pets = new ArrayList<>(PixelPets.REGISTRY.keySet());
                    id = pets.get(world.random.nextInt(pets.size()));
                }
                PetData data = new PetData(id);
                PixelPetItem.initialize(data);
                if (isBreed) {
                    Ability[] abilities = Arrays.stream(PixelPets.REGISTRY.get(id).getAbilities()).filter((a) -> a.getRarity().equals(AbilityRarity.UNUSUAL)).toArray(Ability[]::new);
                    Ability ability = abilities[world.random.nextInt(abilities.length)];
                    data.addAbility(ability);
                }
                petStack.getOrCreateTag().put("PetData", data.toTag());
                ((PlayerEntity) entity).inventory.setStack(slot, petStack);
            }
        }
    }
}
