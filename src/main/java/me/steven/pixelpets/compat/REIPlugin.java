package me.steven.pixelpets.compat;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.items.PetData;
import me.steven.pixelpets.pets.PixelPets;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class REIPlugin implements REIClientPlugin {

    public static final Identifier ID = new Identifier(PixelPetsMod.MOD_ID, "rei_plugin");

    @Override
    public String getPluginProviderName() {
        return "Pixel Pets REI Plugin";
    }

    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        entryRegistry.removeEntry(EntryStacks.of(new ItemStack(PixelPetsMod.PET_ITEM)));
        PixelPets.REGISTRY.forEach((id, pet) -> {
            for (int i = 0; i < pet.getVariants().size(); i++) {
                ItemStack stack = new ItemStack(PixelPetsMod.PET_ITEM);
                PetData data = new PetData(pet.getId(), i);
                stack.setSubNbt("PetData", data.toTag());
                entryRegistry.addEntries(EntryStacks.of(stack));
            }
        });
    }
}
