package me.steven.pixelpets.compat;

import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.items.PetData;
import me.steven.pixelpets.pets.PixelPets;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class REIPlugin implements REIPluginV0 {

    public static final Identifier ID = new Identifier(PixelPetsMod.MOD_ID, "rei_plugin");

    @Override
    public Identifier getPluginIdentifier() {
        return ID;
    }

    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        entryRegistry.removeEntry(EntryStack.create(new ItemStack(PixelPetsMod.PET_ITEM)));
        PixelPets.REGISTRY.forEach((id, pet) -> {
            for (int i = 0; i < pet.getVariants().size(); i++) {
                ItemStack stack = new ItemStack(PixelPetsMod.PET_ITEM);
                PetData data = new PetData(pet.getId(), i);
                stack.putSubTag("PetData", data.toTag());
                entryRegistry.registerEntries(EntryStack.create(stack));
            }
        });
    }
}
