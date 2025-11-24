package com.bekkouame1024.mod.crosshaircustomizer.service;

import com.bekkouame1024.mod.crosshaircustomizer.ConfigManager;
import com.bekkouame1024.mod.crosshaircustomizer.CrosshairCustomizer;
import com.bekkouame1024.mod.crosshaircustomizer.ModConfig;
import com.bekkouame1024.mod.crosshaircustomizer.managers.CrosshairManager;
import com.bekkouame1024.mod.crosshaircustomizer.repository.CrosshairRepository;
import com.bekkouame1024.mod.crosshaircustomizer.utils.FileNameValidator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.UUID;

public class CrosshairService {
    private final ModConfig config;
    private final CrosshairRepository crosshairRepository;
    
    public CrosshairService(CrosshairRepository crosshairRepository) {
        this.config = CrosshairCustomizer.CONFIG;
        this.crosshairRepository = crosshairRepository;
    }
    
    public void importAllCrosshairs() {
        List<String> crosshairFileNames = crosshairRepository.getAllCrosshairFileNames();
        if (crosshairFileNames == null || crosshairFileNames.isEmpty()) {
            CrosshairCustomizer.LOGGER.warn("No crosshair file names were found.");
            return;
        }
        
        crosshairFileNames.forEach(this::importCrosshair);
    }
    
    public void importCrosshair(String fileName) {
        String crosshairName = fileName.substring(0,  fileName.length() - ".png".length());

        boolean alreadyImported = this.config.crosshairs.stream().anyMatch(entry -> entry.file.equals(fileName));
        if (alreadyImported) {
            CrosshairCustomizer.LOGGER.warn("Crosshair already imported: {}", fileName);
            return;
        }

        if (!FileNameValidator.isValidFileName(fileName)) {
            CrosshairCustomizer.LOGGER.warn("Import failed: invalid crosshair name - {}", crosshairName);
            return;
        }
        
        BufferedImage bufferedImage = crosshairRepository.loadCrosshairImage(fileName);

        if (bufferedImage == null) {
            CrosshairCustomizer.LOGGER.warn("Import failed: could not read image - {}", fileName);
            return;
        }
        crosshairRepository.saveCrosshairImage(bufferedImage, fileName);

        ModConfig config = CrosshairCustomizer.CONFIG;
        config.crosshairs.add(new ModConfig.CrosshairEntry(fileName, config.crosshairs.size()));
        ConfigManager.save(config);

        NativeImage nativeImage = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), true);
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                //? if >=1.21 <=1.21.1 {
                /*nativeImage.setColor(x, y, bufferedImage.getRGB(x, y));
                 *///?}

                //? if >=1.21.4 <=1.21.8 {
                nativeImage.setColorArgb(x, y, bufferedImage.getRGB(x, y));
                //?}
            }
        }

        //? if >=1.21 <=1.21.4 {
        /*NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
         *///?}

        //? if >=1.21.6 <=1.21.8 {
        NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> UUID.randomUUID().toString(), nativeImage);
        //?}
        Identifier id = Identifier.of(CrosshairCustomizer.MOD_ID, crosshairName);

        MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
        CrosshairManager.putCrosshair(crosshairName, id);

        CrosshairCustomizer.LOGGER.info("Successfully imported crosshair: {}", fileName);
    }
}
