package me.steven.pixelpets.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.loot.LootTables;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class Config {
    
    public static final Config INSTANCE;

    static {
        Config config;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "pixelpets.json");
        try {
            if (!file.exists()) {
                config = new Config();
                if (file.createNewFile()) Files.write(file.toPath(), gson.toJson(config).getBytes());
            } else {
                config = gson.fromJson(Files.readString(file.toPath()), Config.class);
            }
        } catch (IOException e) {
            config = new Config();
            e.printStackTrace();
        }
        INSTANCE = config;
    }
    public int hatchingTicks = 12000;
    public int breedingTicks = 12000;
    public List<String> lootTablesEggsCanBeFound = Arrays.asList(
            LootTables.ANCIENT_CITY_CHEST.toString(),
            LootTables.ABANDONED_MINESHAFT_CHEST.toString(),
            LootTables.BURIED_TREASURE_CHEST.toString(),
            LootTables.SHIPWRECK_TREASURE_CHEST.toString()
    );
    public List<String> lootTablesStatueCanBeFound = Arrays.asList(
            LootTables.HERO_OF_THE_VILLAGE_FARMER_GIFT_GAMEPLAY.toString(),
            LootTables.HERO_OF_THE_VILLAGE_LIBRARIAN_GIFT_GAMEPLAY.toString(),
            LootTables.HERO_OF_THE_VILLAGE_SHEPHERD_GIFT_GAMEPLAY.toString()
    );
}
