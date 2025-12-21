package com.bekkouame1024.mod.crosshaircustomizer.config;

import com.bekkouame1024.mod.crosshaircustomizer.CrosshairCustomizer;
import com.bekkouame1024.mod.crosshaircustomizer.config.migration.ConfigMigrator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/" + CrosshairCustomizer.MOD_ID + "/" + "config.json");
    private static final int LATEST_VERSION = 2;

    public static ModConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                ConfigMigrator migrator = new ConfigMigrator(GSON, LATEST_VERSION);
                
                ConfigMigrator.MigrationResult result = migrator.migrate(root);
                
                if (result.migrated) {
                    CrosshairCustomizer.LOGGER.info("Config file migrated to the latest version.");
                    save(result.config);
                }
                
                return result.config;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return new ModConfig();
    }

    public static void save(ModConfig config) {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
