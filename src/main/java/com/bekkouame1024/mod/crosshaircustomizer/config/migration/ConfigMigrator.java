package com.bekkouame1024.mod.crosshaircustomizer.config.migration;

import com.bekkouame1024.mod.crosshaircustomizer.CrosshairCustomizer;
import com.bekkouame1024.mod.crosshaircustomizer.config.ModConfig;
import com.bekkouame1024.mod.crosshaircustomizer.config.migration.dto.ModConfigV1;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ConfigMigrator {
    private final Gson gson;
    private final int latestVersion;
    
    public ConfigMigrator(Gson gson, int latestVersion) {
        this.gson = gson;
        this.latestVersion = latestVersion;
    }

    public MigrationResult migrate(JsonObject root) {
        int version = root.has("version") ? root.get("version").getAsInt() : 1;

        ModConfig config = null;
        JsonObject current = root;
        boolean migrated = false;
        
        while (version < latestVersion) {
            config = migrateStep(current, version);
            current = gson.toJsonTree(config).getAsJsonObject();
            version++;
            migrated = true;
        }
        
        if (config == null) {
            config = gson.fromJson(root, ModConfig.class);
        }

        return new MigrationResult(migrated, config);
    }
    
    private ModConfig migrateStep(JsonObject root, int version) {
        return switch (version) {
            case 1 -> migrateV1ToV2(root);
            default -> gson.fromJson(root, ModConfig.class);
        };
    }
    
    private ModConfig migrateV1ToV2(JsonObject root) {
        ModConfigV1 oldConfig = gson.fromJson(root, ModConfigV1.class);

        ModConfig newConfig = new ModConfig();
        newConfig.version = latestVersion;

        CrosshairCustomizer.LOGGER.info(String.valueOf(oldConfig.currentCrosshair));
        CrosshairCustomizer.LOGGER.info(String.valueOf(oldConfig.currentTargetCrosshair));
        CrosshairCustomizer.LOGGER.info(root.toString());
        
        if (oldConfig.currentCrosshair == null) {
            newConfig.currentCrosshair = null;
            CrosshairCustomizer.LOGGER.info("a");
        } else {
            newConfig.currentCrosshair = oldConfig.currentCrosshair.toString();
            CrosshairCustomizer.LOGGER.info("b");
        }

        if (oldConfig.currentTargetCrosshair == null) {
            newConfig.currentTargetCrosshair = null;
            CrosshairCustomizer.LOGGER.info("c");
        } else {
            newConfig.currentTargetCrosshair = oldConfig.currentTargetCrosshair.toString();
            CrosshairCustomizer.LOGGER.info("d");
        }

        if (oldConfig.crosshairs != null) {
            oldConfig.crosshairs.forEach(e ->
                    newConfig.crosshairs.add(
                            new ModConfig.CrosshairEntry(e.file, e.order)
                    )
            );
        }

        return newConfig;
    }

    public static class MigrationResult {
        public final boolean migrated;
        public final ModConfig config;

        public MigrationResult(boolean migrated, ModConfig config) {
            this.migrated = migrated;
            this.config = config;
        }
    }
}
