package com.bekkouame1024.mod.crosshaircustomizer.repository;

import com.bekkouame1024.mod.crosshaircustomizer.CrosshairCustomizer;
import com.bekkouame1024.mod.crosshaircustomizer.utils.FileNameValidator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CrosshairRepository {
    private static final String CROSSHAIR_DIRECTORY = "config/" + CrosshairCustomizer.MOD_ID + "/crosshairs";
    
    public void saveCrosshairImage(BufferedImage image, String fileName) {
        File outputFile = new File(CROSSHAIR_DIRECTORY, fileName);
        try {
            ImageIO.write(image, "PNG", outputFile);
        } catch (IOException e) {
            CrosshairCustomizer.LOGGER.error("Failed to save crosshair image: {}", fileName, e);
        }
    }
    
    public BufferedImage loadCrosshairImage(String fileName) {
        File file = new File(CROSSHAIR_DIRECTORY, fileName);
        
        if (!file.isFile() || !file.getName().endsWith(".png")) {
            return null;
        }
        
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            CrosshairCustomizer.LOGGER.error("Failed to load crosshair image: {}", file.getName(), e);
            return null;
        }
    }
    
    public List<String> getAllCrosshairFileNames() {
        File[] files = new File(CROSSHAIR_DIRECTORY).listFiles();
        if(files == null){
            return null;
        }
        
        List<String> list = new ArrayList<>();
        for (File file : Objects.requireNonNull(files)) {
            if (!file.isFile() || !file.getName().endsWith(".png")) {
                continue;
            }
            
            if(!FileNameValidator.isValidFileName(file.getName())){
                continue;
            }
            
            list.add(file.getName());
        }
        
        return list;
    }
    
    public Path getCrosshairDirectoryPath() {
        return Path.of(CROSSHAIR_DIRECTORY);
    }
}
