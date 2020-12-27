package me.steven.pixelpets;

import me.steven.pixelpets.items.PixelPetItem;
import me.steven.pixelpets.pets.PixelPets;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PixelPetsMod implements ModInitializer  {

    public static final String MOD_ID = "pixelpets";

    public static final Item PIG_PET =
            Registry.register(Registry.ITEM, new Identifier(MOD_ID, "pig"), new PixelPetItem(PixelPets.PIG, new Item.Settings()));

    @Override
    public void onInitialize() {

    }
}
