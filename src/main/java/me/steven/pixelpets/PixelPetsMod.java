package me.steven.pixelpets;

import me.steven.pixelpets.items.PixelPetItem;
import me.steven.pixelpets.json.abilities.AbilityResourceReloadListener;
import me.steven.pixelpets.json.pets.PetResourceReloadListener;
import me.steven.pixelpets.pets.PixelPets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PixelPetsMod implements ModInitializer  {

    public static final String MOD_ID = "pixelpets";

    public static final Item PET_ITEM =
            Registry.register(Registry.ITEM, new Identifier(MOD_ID, "pet"), new PixelPetItem(new Item.Settings().maxCount(1)));

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new AbilityResourceReloadListener());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new PetResourceReloadListener());
    }
}
