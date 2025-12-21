package com.bekkouame1024.mod.crosshaircustomizer;

import com.bekkouame1024.mod.crosshaircustomizer.config.ConfigManager;
import com.bekkouame1024.mod.crosshaircustomizer.config.ModConfig;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrosshairCustomizer implements ModInitializer {
	public static final String MOD_ID = "crosshaircustomizer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ModConfig CONFIG;

	@Override
	public void onInitialize() {
		CONFIG = ConfigManager.load();
	}
}