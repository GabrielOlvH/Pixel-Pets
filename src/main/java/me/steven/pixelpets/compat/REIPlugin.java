package me.steven.pixelpets.compat;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.housing.Housing;
import me.steven.pixelpets.housing.HousingData;
import me.steven.pixelpets.pets.PetData;
import me.steven.pixelpets.pets.PixelPets;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class REIPlugin implements REIClientPlugin {

    public static final Identifier ID = new Identifier(PixelPetsMod.MOD_ID, "rei_plugin");

    @Override
    public String getPluginProviderName() {
        return "Pixel Pets REI Plugin";
    }

    @Override
    public void registerEntries(EntryRegistry entryRegistry) {

    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.add(DefaultInformationDisplay
                .createFromEntry(EntryStacks.of(PixelPetsMod.OVERWORLD_EGG_ITEM), Text.literal("Eggs"))
                .lines(Text.literal("Eggs can be found in chests in places such as Ancient Cities, Mineshafts, Buried Treasures and Shipwrecks.")));

        registry.add(DefaultInformationDisplay
                .createFromEntry(EntryStacks.of(PixelPetsMod.ABILITY_REROLL_ITEM), Text.literal("Goddess Statue"))
                .lines(Text.literal("This statue can be rewarded to the player by Farmer, Librarian and Shepherd villagers after defeating a raid.")));
    }
}
