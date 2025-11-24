package com.bekkouame1024.mod.crosshaircustomizer.managers;

import com.bekkouame1024.mod.crosshaircustomizer.ConfigManager;
import com.bekkouame1024.mod.crosshaircustomizer.CrosshairCustomizer;
import com.bekkouame1024.mod.crosshaircustomizer.ModConfig;
import com.bekkouame1024.mod.crosshaircustomizer.model.MenuType;
import com.bekkouame1024.mod.crosshaircustomizer.repository.CrosshairRepository;
import com.bekkouame1024.mod.crosshaircustomizer.utils.FileNameValidator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

public class CrosshairManager {
    private static final Map<String, Identifier> CROSSHAIRS = new HashMap<>();
    private static MenuType currentMenuType = MenuType.MAIN;
    
    public static Identifier getCrosshair(String name) {
        return CROSSHAIRS.get(name);
    }
    
    public static List<String> getCrosshairNames() {
        return List.copyOf(CROSSHAIRS.keySet());
    }

    public static void setCurrentCrosshair(String name) {
        ModConfig config = CrosshairCustomizer.CONFIG;
        
        Identifier id = CROSSHAIRS.get(name);
        if (id != null) {
            config.currentCrosshair = id;
            ConfigManager.save(config);
        } else {
            CrosshairCustomizer.LOGGER.warn("Crosshair not found: {}", name);
        }
    }
    
    public static void setCurrentTargetCrosshair(String name) {
        ModConfig config = CrosshairCustomizer.CONFIG;
        
        Identifier id = CROSSHAIRS.get(name);
        if (id != null) {
            config.currentTargetCrosshair = id;
            ConfigManager.save(config);
        } else {
            CrosshairCustomizer.LOGGER.warn("Crosshair not found: {}", name);
        }
    }
    
    public static void resetCurrentCrosshairToDefault(){
        ModConfig config = CrosshairCustomizer.CONFIG;
        config.currentCrosshair = null;
        ConfigManager.save(config);
    }
    
    public static void resetCurrentTargetCrosshairToDefault(){
        ModConfig config = CrosshairCustomizer.CONFIG;
        config.currentTargetCrosshair = null;
        ConfigManager.save(config);
    }
    
    public static void putCrosshair(String name, Identifier id) {
        CROSSHAIRS.put(name, id);
    }

    public static void reloadCrosshairs() {
        ModConfig config = CrosshairCustomizer.CONFIG;
        ConfigManager.load();
        
        CROSSHAIRS.clear();
        
        checkCrosshairExists();
        
        if(config.crosshairs == null || config.crosshairs.isEmpty()) {
            CrosshairCustomizer.LOGGER.info("No crosshairs defined in the configuration.");
            return;
        }
        
        for(ModConfig.CrosshairEntry entry : config.crosshairs) {
            String name = entry.file.substring(0, entry.file.length() - ".png".length());
            
            BufferedImage bufferedImage = new CrosshairRepository().loadCrosshairImage(entry.file);
            if (bufferedImage == null) {
                CrosshairCustomizer.LOGGER.warn("Failed to load crosshair image: {}", entry.file);
                continue;
            }

            NativeImage image = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), true);
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    //? if >=1.21 <=1.21.1 {
                    /*image.setColor(x, y, bufferedImage.getRGB(x, y));
                     *///?}

                    //? if >=1.21.4 <=1.21.8 {
                    image.setColorArgb(x, y, bufferedImage.getRGB(x, y));
                    //?}
                }
            }

            //? if >=1.21 <=1.21.4 {
            /*NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
             *///?}

            //? if >=1.21.6 <=1.21.8 {
            NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> UUID.randomUUID().toString(), image);
            //?}
            Identifier id = Identifier.of(CrosshairCustomizer.MOD_ID, name);

            MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
            CROSSHAIRS.put(name, id);
        }
    }
    
    public static void deleteCrosshair(String name) {
        ModConfig config = CrosshairCustomizer.CONFIG;
        
        Identifier id = CROSSHAIRS.get(name);
        if (id == null) {
            CrosshairCustomizer.LOGGER.warn("Crosshair not found: {}", name);
            return;
        }
        
        File file = new File("config/" + CrosshairCustomizer.MOD_ID + "/crosshairs", name + ".png");
        if (file.exists() && !file.delete()) {
            CrosshairCustomizer.LOGGER.error("Failed to delete crosshair file: {}", file.getAbsolutePath());
            return;
        }
        
        config.crosshairs.removeIf(entry -> entry.file.equals(name + ".png"));
        if (config.currentCrosshair != null && config.currentCrosshair.equals(id)) {
            config.currentCrosshair = null;
        }
        ConfigManager.save(config);
        
        AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(id);
        if (texture instanceof NativeImageBackedTexture nativeImageBackedTexture) {
            nativeImageBackedTexture.close();
        }
        MinecraftClient.getInstance().getTextureManager().destroyTexture(id);
        
        CROSSHAIRS.remove(name);
        
        CrosshairCustomizer.LOGGER.info("Deleted crosshair: {}", name);
    }
    
    private static void checkCrosshairExists() {
        ModConfig config = CrosshairCustomizer.CONFIG;
        Iterator<ModConfig.CrosshairEntry> iterator = config.crosshairs.iterator();
        while (iterator.hasNext()) {
            ModConfig.CrosshairEntry entry = iterator.next();
            File file = new File("config/" + CrosshairCustomizer.MOD_ID + "/crosshairs", entry.file);
            if (file.exists()) {
                continue;
            }
            iterator.remove();
            ConfigManager.save(config);
            CrosshairCustomizer.LOGGER.warn("Removed missing crosshair from config: {}", entry.file);
        }
    }
    
    public static MenuType getCurrentMenuType() {
        return currentMenuType;
    }
    
    public static void setCurrentMenuType(MenuType menuType) {
        currentMenuType = menuType;
    }
}
