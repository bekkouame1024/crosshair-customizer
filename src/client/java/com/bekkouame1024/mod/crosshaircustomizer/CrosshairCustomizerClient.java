package com.bekkouame1024.mod.crosshaircustomizer;

import com.bekkouame1024.mod.crosshaircustomizer.input.InputState;
import com.bekkouame1024.mod.crosshaircustomizer.managers.CrosshairManager;
import com.bekkouame1024.mod.crosshaircustomizer.screen.CanvasScreen;
import com.bekkouame1024.mod.crosshaircustomizer.utils.KeyBindRegister;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

public class CrosshairCustomizerClient implements ClientModInitializer {
	private static boolean initialized = false;
	
	@Override
	public void onInitializeClient() {
		KeyBindRegister.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!initialized) {
				CrosshairManager.reloadCrosshairs();
				initialized = true;
			}
		});

		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if(!(screen instanceof CanvasScreen)) {
				return;
			}
			InputState.setMouseDown(false, 0);
			
			ScreenMouseEvents.beforeMouseClick(screen).register((scr, mouseX, mouseY, button) -> {
                InputState.setMouseDown(true, button);
			});
			ScreenMouseEvents.afterMouseRelease(screen).register((scr, mouseX, mouseY, button) -> {
                InputState.setMouseDown(false, button);
			});
		});
	}
}