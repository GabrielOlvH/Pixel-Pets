package me.steven.pixelpets;

import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.commands.PixelPetsCommands;
import me.steven.pixelpets.items.HousingItem;
import me.steven.pixelpets.items.PixelPetEggItem;
import me.steven.pixelpets.items.PixelPetItem;
import me.steven.pixelpets.json.abilities.AbilityResourceReloadListener;
import me.steven.pixelpets.json.housing.HousingResourceReloadListener;
import me.steven.pixelpets.json.pets.PetResourceReloadListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;

import java.util.Arrays;
import java.util.List;

public class PixelPetsMod implements ModInitializer {

    public static final String MOD_ID = "pixelpets";

    public static final Identifier SHOW_ITEM_PACKET = new Identifier(MOD_ID, "show_item");

    public static final Item PET_ITEM =
            Registry.register(Registries.ITEM, new Identifier(MOD_ID, "pet"), new PixelPetItem(new Item.Settings().maxCount(1)));
    public static final Item OVERWORLD_EGG_ITEM =
            Registry.register(Registries.ITEM, new Identifier(MOD_ID, "overworld_egg"), new PixelPetEggItem(new Identifier(MOD_ID, "overworld"), 0x33cc33));

 /*   public static final Item NETHER_EGG_ITEM =
            Registry.register(Registries.ITEM, new Identifier(MOD_ID, "nether_egg"), new PixelPetEggItem(new Identifier(MOD_ID, "nether"), 0xcc0000));

    public static final Item END_EGG_ITEM =
            Registry.register(Registries.ITEM, new Identifier(MOD_ID, "the_end_egg"), new PixelPetEggItem(new Identifier(MOD_ID, "the_end"), 0x660066));
    public static final Item HOUSING_ITEM =
            Registry.register(Registries.ITEM, new Identifier(MOD_ID, "housing"), new HousingItem(new Item.Settings().maxCount(1)));*/

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new AbilityResourceReloadListener());
        Abilities.loadBuiltins();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new PetResourceReloadListener());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new HousingResourceReloadListener());

        /*TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 3, factories -> {
            factories.add((entity, random) -> new TradeOffer(new ItemStack(Items.HONEY_BOTTLE), new ItemStack(Items.EMERALD, 2), new ItemStack(OVERWORLD_EGG_ITEM), 3, 1, 5));
        });*/


        List<Identifier> ids = Arrays.asList(LootTables.ANCIENT_CITY_CHEST,
                LootTables.ABANDONED_MINESHAFT_CHEST,
                LootTables.BURIED_TREASURE_CHEST,
                LootTables.SHIPWRECK_TREASURE_CHEST);
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (source.isBuiltin() && ids.contains(id)) {
                tableBuilder.modifyPools(pool -> pool.with(ItemEntry.builder(OVERWORLD_EGG_ITEM).weight(1)));
            }
        });

        PixelPetsCommands.register();
    }
}
