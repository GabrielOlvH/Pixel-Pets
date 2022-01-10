package me.steven.pixelpets;

import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.commands.PixelPetsCommands;
import me.steven.pixelpets.items.HousingItem;
import me.steven.pixelpets.items.PixelPetEggItem;
import me.steven.pixelpets.items.PixelPetItem;
import me.steven.pixelpets.json.abilities.AbilityResourceReloadListener;
import me.steven.pixelpets.json.housing.HousingResourceReloadListener;
import me.steven.pixelpets.json.pets.PetResourceReloadListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PixelPetsMod implements ModInitializer  {

    public static final String MOD_ID = "pixelpets";

    public static final Identifier SHOW_ITEM_PACKET = new Identifier(MOD_ID, "show_item");

    public static final Item PET_ITEM =
            Registry.register(Registry.ITEM, new Identifier(MOD_ID, "pet"), new PixelPetItem(new Item.Settings().maxCount(1)));
    public static final Item EGG_ITEM =
            Registry.register(Registry.ITEM, new Identifier(MOD_ID, "egg"), new PixelPetEggItem());
    public static final Item HOUSING_ITEM =
            Registry.register(Registry.ITEM, new Identifier(MOD_ID, "housing"), new HousingItem(new Item.Settings().maxCount(1)));

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new AbilityResourceReloadListener());
        Abilities.loadBuiltins();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new PetResourceReloadListener());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new HousingResourceReloadListener());

        PixelPetsCommands.register();
    }
}
