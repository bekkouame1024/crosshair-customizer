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
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import com.bekkouame1024.mod.crosshaircustomizer.model.MenuType;

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
        boolean saved = crosshairRepository.saveCrosshairImage(bufferedImage, fileName);
        if (!saved) {
            CrosshairCustomizer.LOGGER.warn("Import failed: could not save image - {}", fileName);
            return;
        }

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

    public static class SaveResult {
        private final boolean ok;
        private final String message;

        private SaveResult(boolean ok, String message) {
            this.ok = ok;
            this.message = message;
        }

        public static SaveResult ok() { return new SaveResult(true, "Saved"); }
        public static SaveResult fail(String message) { return new SaveResult(false, message); }

        public boolean isOk() { return ok; }
        public String getMessage() { return message; }
    }

    public SaveResult saveCrosshair(String name, BufferedImage image) {
        String fileName = name + ".png";

        if (!FileNameValidator.isValidFileName(fileName)) {
            return SaveResult.fail("Invalid file name: " + name);
        }

        Path dir = crosshairRepository.getCrosshairDirectoryPath();
        File outputFile = dir.resolve(fileName).toFile();
        if (outputFile.exists()) {
            return SaveResult.fail("File already exists: " + outputFile.getName());
        }
        
        boolean saved = crosshairRepository.saveCrosshairImage(image, fileName);
        if (!saved) {
            return SaveResult.fail("Failed to save file: " + outputFile.getName());
        }

        ModConfig config = CrosshairCustomizer.CONFIG;
        config.crosshairs.add(new ModConfig.CrosshairEntry(fileName, config.crosshairs.size()));
        ConfigManager.save(config);

        CrosshairManager.reloadCrosshairs();

        return SaveResult.ok();
    }

    public int getSelectedIndex(MenuType menuType) {
        if (menuType == MenuType.MAIN) {
            if (this.config.currentCrosshair == null) return -1;
            for (int i = 0; i < this.config.crosshairs.size(); i++) {
                if (this.config.crosshairs.get(i).file.replace(".png", "").equals(this.config.currentCrosshair.getPath())) {
                    return i;
                }
            }
        } else if (menuType == MenuType.TARGET) {
            if (this.config.currentTargetCrosshair == null) return -1;
            for (int i = 0; i < this.config.crosshairs.size(); i++) {
                if (this.config.crosshairs.get(i).file.replace(".png", "").equals(this.config.currentTargetCrosshair.getPath())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean swapCrosshair(int from, int to) {
        if (from < 0 || to < 0 || from >= this.config.crosshairs.size() || to >= this.config.crosshairs.size()) return false;
        ModConfig.CrosshairEntry temp = this.config.crosshairs.get(from);
        this.config.crosshairs.set(from, this.config.crosshairs.get(to));
        this.config.crosshairs.set(to, temp);

        for (int i = 0; i < this.config.crosshairs.size(); i++) {
            this.config.crosshairs.get(i).order = i;
        }
        ConfigManager.save(this.config);
        return true;
    }

    public int deleteCrosshairs(List<String> names) {
        int deleted = 0;
        for (String name : names) {
            CrosshairManager.deleteCrosshair(name);
            deleted++;
        }
        return deleted;
    }

    public void setCurrentCrosshair(String name, MenuType menuType) {
        if (menuType == MenuType.MAIN) {
            if (name == null) {
                CrosshairManager.resetCurrentCrosshairToDefault();
            } else {
                CrosshairManager.setCurrentCrosshair(name);
            }
        } else if (menuType == MenuType.TARGET) {
            if (name == null) {
                CrosshairManager.resetCurrentTargetCrosshairToDefault();
            } else {
                CrosshairManager.setCurrentTargetCrosshair(name);
            }
        }
    }
}
