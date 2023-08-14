package me.steven.pixelpets.compat;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.housing.Housing;
import me.steven.pixelpets.housing.HousingData;
import me.steven.pixelpets.pets.PetData;
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
                data.update(stack);
                entryRegistry.addEntries(EntryStacks.of(stack));
            }
        });

        entryRegistry.removeEntry(EntryStacks.of(new ItemStack(PixelPetsMod.HOUSING_ITEM)));

        Housing.REGISTRY.forEach((id, housing) -> {
                ItemStack stack = new ItemStack(PixelPetsMod.HOUSING_ITEM);
                HousingData data = new HousingData(id);
                stack.setSubNbt("HousingData", data.toNbt());
                entryRegistry.addEntries(EntryStacks.of(stack));
        });
    }
}
