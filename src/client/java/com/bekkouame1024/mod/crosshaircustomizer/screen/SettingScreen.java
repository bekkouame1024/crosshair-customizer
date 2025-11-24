package com.bekkouame1024.mod.crosshaircustomizer.screen;

import com.bekkouame1024.mod.crosshaircustomizer.*;
import com.bekkouame1024.mod.crosshaircustomizer.input.InputState;
import com.bekkouame1024.mod.crosshaircustomizer.managers.CrosshairManager;
import com.bekkouame1024.mod.crosshaircustomizer.model.MenuType;
import com.bekkouame1024.mod.crosshaircustomizer.repository.CrosshairRepository;
import com.bekkouame1024.mod.crosshaircustomizer.service.CrosshairService;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.container.*;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.core.Insets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.*;

public class SettingScreen extends BaseUIModelScreen<FlowLayout> {
    private static final int COLUMNS = 6;
    private static final int CROSSHAIR_SIZE = 40;
    private static final int CROSSHAIR_CONTAINER_COLOR = 0xE1101010;
    
    private ModConfig config;
    
    private Map<FlowLayout, String> crosshairItems = new HashMap<>();

    public SettingScreen() {
        super(FlowLayout.class, DataSource.asset(Identifier.of(CrosshairCustomizer.MOD_ID, "settings")));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        config = CrosshairCustomizer.CONFIG;
        
        CrosshairManager.setCurrentMenuType(MenuType.MAIN);
        addTopUIComponents(rootComponent);
        buildCrosshairGrid(rootComponent);
        addUIComponents(rootComponent);
    }

    private void buildCrosshairGrid(FlowLayout rootComponent) {
        rootComponent.childById(ScrollContainer.class, "scrollContainer")
                .surface(Surface.blur(1f, 1f).and(Surface.flat(0xB4101010)).and(new TopOutlineSurface(0xD0666666)));
        
        List<ModConfig.CrosshairEntry> entries = new ArrayList<>(this.config.crosshairs);
        entries.sort(Comparator.comparingInt(e -> e.order));
        List<String> crosshairNames = new ArrayList<>();
        for (ModConfig.CrosshairEntry entry : entries) {
            crosshairNames.add(entry.file.replace(".png", ""));
        }

        int itemCount = crosshairNames.size();
        int rows = (int) Math.ceil((double) itemCount / COLUMNS);

        GridLayout crosshairGrid = Containers.grid(Sizing.content(), Sizing.content(), rows, COLUMNS);
        crosshairGrid.margins(Insets.of(5))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        for (int i = 0; i < itemCount; i++) {
            int row = i / COLUMNS;
            int col = i % COLUMNS;

            ParentComponent crosshairItem = createCrosshairItem(crosshairNames.get(i));
            crosshairGrid.child(crosshairItem, row, col);
        }

        rootComponent.childById(FlowLayout.class, "gridContainer").child(crosshairGrid);
    }

    private int getSelectedIndex() {
        if(CrosshairManager.getCurrentMenuType() == MenuType.MAIN) {
            if (this.config.currentCrosshair == null) return -1;
            for (int i = 0; i < this.config.crosshairs.size(); i++) {
                if (this.config.crosshairs.get(i).file.replace(".png", "").equals(this.config.currentCrosshair.getPath())) {
                    return i;
                }
            }
        }else if (CrosshairManager.getCurrentMenuType() == MenuType.TARGET) {
            if (this.config.currentTargetCrosshair == null) return -1;
            for (int i = 0; i < this.config.crosshairs.size(); i++) {
                if (this.config.crosshairs.get(i).file.replace(".png", "").equals(this.config.currentTargetCrosshair.getPath())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void swapCrosshair(int from, int to) {
        if (from < 0 || to < 0 || from >= this.config.crosshairs.size() || to >= this.config.crosshairs.size()) return;
        ModConfig.CrosshairEntry temp = this.config.crosshairs.get(from);
        this.config.crosshairs.set(from, this.config.crosshairs.get(to));
        this.config.crosshairs.set(to, temp);

        for (int i = 0; i < this.config.crosshairs.size(); i++) {
            this.config.crosshairs.get(i).order = i;
        }
        ConfigManager.save(this.config);
    }

    private void addTopUIComponents(FlowLayout rootComponent) {
        rootComponent.childById(FlowLayout.class, "topControlContainer")
                .surface(Surface.flat(0xB4101010)
                        .and(Surface.blur(1, 1))
                );


        rootComponent.childById(FlowLayout.class, "upOrderButton").mouseDown().subscribe((mouseX, mouseY, button) -> {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            int idx = getSelectedIndex();
            if (idx > 0) {
                swapCrosshair(idx, idx - 1);
                reloadCrosshairGrid(rootComponent);
            }
            return true;
        });

        rootComponent.childById(FlowLayout.class, "downOrderButton").mouseDown().subscribe((mouseX, mouseY, button) -> {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            int idx = getSelectedIndex();
            ModConfig config = CrosshairCustomizer.CONFIG;
            if (idx != -1 && idx < config.crosshairs.size() - 1) {
                swapCrosshair(idx, idx + 1);
                reloadCrosshairGrid(rootComponent);
            }
            return true;
        });
        
        rootComponent.childById(FlowLayout.class, "deleteButton").mouseDown().subscribe((mouseX, mouseY, button) -> {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            
            crosshairItems.forEach((item, name) -> {
                if(item == null) return;
                
                if(item.children().getFirst() instanceof SelectBoxComponent selectBox) {
                    if(!selectBox.checked()) return;
                    
                    CrosshairManager.deleteCrosshair(name);
                }
            });

            reloadCrosshairGrid(rootComponent);
            
            return true;
        });
    }

    private void addUIComponents(FlowLayout rootComponent) {
        rootComponent.childById(FlowLayout.class, "changeMenuButton").mouseDown().subscribe((mouseX, mouseY, button) -> {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if (CrosshairManager.getCurrentMenuType() == MenuType.MAIN) {
                CrosshairManager.setCurrentMenuType(MenuType.TARGET);
                
                rootComponent.childById(FlowLayout.class, "changeMenuButton").tooltip(Text.of("Change Menu(Current: Target)"));
                
                Identifier currentTargetCrosshair = this.config.currentTargetCrosshair;
                crosshairItems.forEach((item, name) -> {
                    if (currentTargetCrosshair != null && name.equals(currentTargetCrosshair.getPath())) {
                        item.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR).and(Surface.outline(0xD0FFFFFF)));
                    } else {
                        item.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR));
                    }
                });
            } else if (CrosshairManager.getCurrentMenuType() == MenuType.TARGET) {
                CrosshairManager.setCurrentMenuType(MenuType.MAIN);

                rootComponent.childById(FlowLayout.class, "changeMenuButton").tooltip(Text.of("Change Menu(Current: Main)"));
                
                Identifier currentCrosshair = this.config.currentCrosshair;
                crosshairItems.forEach((item, name) -> {
                    if (currentCrosshair != null && name.equals(currentCrosshair.getPath())) {
                        item.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR).and(Surface.outline(0xD0FFFFFF)));
                    } else {
                        item.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR));
                    }
                });
            }

            crosshairItems.forEach((item, name) -> {
                if(item.children().getFirst() instanceof SelectBoxComponent selectBox) {
                    selectBox.checked(false);
                }
            });

            return true;
        });
        
        rootComponent.childById(FlowLayout.class, "controlContainer")
                .surface(Surface.flat(0xB4101010)
                        .and(Surface.blur(1, 1))
                        .and(new TopOutlineSurface(0xD0666666))
                );

        rootComponent.childById(FlowLayout.class, "addButton").mouseDown().subscribe((mouseX, mouseY, button) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            client.setScreen(new CanvasScreen());

            return true;
        });

        rootComponent.childById(FlowLayout.class, "importButton").mouseDown().subscribe((mouseX, mouseY, button) -> {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            openImportOverlay(rootComponent);

            return true;
        });

        rootComponent.childById(FlowLayout.class, "openFolderButton").mouseDown().subscribe((mouseX, mouseY, button) -> {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            new Thread(() -> Util.getOperatingSystem().open(new CrosshairRepository().getCrosshairDirectoryPath())).start();

            return true;
        });

        rootComponent.childById(FlowLayout.class, "defaultButton").mouseDown().subscribe((mouseX, mouseY, button) -> {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            
            if(CrosshairManager.getCurrentMenuType() == MenuType.MAIN) {
                crosshairItems.forEach((item, name) -> item.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR)));
                CrosshairManager.resetCurrentCrosshairToDefault();
            }else if(CrosshairManager.getCurrentMenuType() == MenuType.TARGET) {
                crosshairItems.forEach((item, name) -> item.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR)));
                CrosshairManager.resetCurrentTargetCrosshairToDefault();
            }

            return true;
        });

        rootComponent.childById(FlowLayout.class, "reloadButton").mouseDown().subscribe((mouseX, mouseY, button) -> {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            reloadCrosshairGrid(rootComponent);

            return true;
        });
    }

    private void reloadCrosshairGrid(FlowLayout rootComponent) {
        FlowLayout gridContainer = rootComponent.childById(FlowLayout.class, "gridContainer");
        if (gridContainer != null) {
            new ArrayList<>(gridContainer.children()).forEach(gridContainer::removeChild);
        }
        crosshairItems.clear();

        CrosshairManager.reloadCrosshairs();

        buildCrosshairGrid(rootComponent);
    }

    private FlowLayout createCrosshairItem(String crosshairName) {
        FlowLayout crosshairContainer = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(new SelectBoxComponent(8))
                .child(
                        Components
                                .texture(CrosshairManager.getCrosshair(crosshairName), 0, 0, 15, 15, 15, 15)
                                .sizing(Sizing.fixed(CROSSHAIR_SIZE))
                                .margins(Insets.of(5))
                );

        crosshairContainer
                .margins(Insets.of(2))
                .padding(Insets.of(5))
                .surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR))
                .tooltip(Text.literal(crosshairName));
        
        if(CrosshairManager.getCurrentMenuType() == MenuType.MAIN) {
            Identifier currentCrosshair = this.config.currentCrosshair;
            if (currentCrosshair != null && crosshairName.equals(currentCrosshair.getPath())) {
                crosshairContainer.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR).and(Surface.outline(0xD0FFFFFF)));
            }
        } else if (CrosshairManager.getCurrentMenuType() == MenuType.TARGET) {
            Identifier currentTargetCrosshair = this.config.currentTargetCrosshair;
            if (currentTargetCrosshair != null && crosshairName.equals(currentTargetCrosshair.getPath())) {
                crosshairContainer.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR).and(Surface.outline(0xD0FFFFFF)));
            }
        }

        

        crosshairContainer.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (InputState.isShiftDown()) {
                if (crosshairContainer.children().getFirst() instanceof SelectBoxComponent selectBox) {
                    selectBox.checked(!selectBox.checked());
                }
            } else {
                if(CrosshairManager.getCurrentMenuType() == MenuType.MAIN) {
                    crosshairItems.forEach((item, name) -> item.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR)));
                    crosshairContainer.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR).and(Surface.outline(0xD0FFFFFF)));
                    CrosshairManager.setCurrentCrosshair(crosshairName);
                }else if (CrosshairManager.getCurrentMenuType() == MenuType.TARGET) {
                    crosshairItems.forEach((item, name) -> item.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR)));
                    crosshairContainer.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR).and(Surface.outline(0xD0FFFFFF)));
                    CrosshairManager.setCurrentTargetCrosshair(crosshairName);
                }
            }
            
            return true;
        });

        crosshairItems.put(crosshairContainer, crosshairName);

        return crosshairContainer;
    }

    private void openImportOverlay(FlowLayout rootComponent) {
        CrosshairRepository crosshairRepository = new CrosshairRepository();
        CrosshairService crosshairService = new CrosshairService(crosshairRepository);
        
        FlowLayout importContainer = Containers.verticalFlow(Sizing.fixed(200), Sizing.fixed(180));
        importContainer.child(Components.label(Text.literal("Please select the png file to import.")).margins(Insets.of(5)))
                .surface(Surface.blur(1f, 1f).and(Surface.flat(0xD4101010).and(Surface.outline(0xD0666666))))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER).zIndex(5);

        OverlayContainer<FlowLayout> overlay = Containers.overlay(importContainer);
        overlay.surface(Surface.BLANK);

        DropdownComponent dropdown = Components.dropdown(Sizing.fill());
        dropdown.margins(Insets.bottom(5)).surface(Surface.flat(0xD4101010));

        ScrollContainer<DropdownComponent> scrollContainer = Containers.verticalScroll(Sizing.fixed(150), Sizing.fixed(120), dropdown);
        scrollContainer.surface(Surface.BLANK);

        List<String> fileNames = crosshairRepository.getAllCrosshairFileNames();
        if (fileNames == null || fileNames.isEmpty()) {
            importContainer.child(scrollContainer);
            rootComponent.child(overlay);
            return;
        }

        fileNames.forEach(fileName -> {
            dropdown.button(Text.literal(fileName), button -> {

                crosshairService.importCrosshair(fileName);

                CrosshairManager.reloadCrosshairs();

                MinecraftClient client = MinecraftClient.getInstance();
                client.setScreen(new SettingScreen());
            });
        });

        importContainer.child(scrollContainer);

        importContainer.child(
                Components.button(Text.literal("Import All"), button -> {
                    crosshairService.importAllCrosshairs();

                    CrosshairManager.reloadCrosshairs();
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new SettingScreen());
                }).textShadow(false)
                        .renderer(ButtonComponent.Renderer.flat(0xD0101010, 0xD0101010, 0xD0101010))
                        .margins(Insets.of(2))
        );

        rootComponent.child(overlay);
    }
}
