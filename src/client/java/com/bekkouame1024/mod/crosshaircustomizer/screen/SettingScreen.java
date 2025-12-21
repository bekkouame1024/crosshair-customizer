package com.bekkouame1024.mod.crosshaircustomizer.screen;

import com.bekkouame1024.mod.crosshaircustomizer.*;
import com.bekkouame1024.mod.crosshaircustomizer.config.ModConfig;
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
    private static final UiComponentFactory.ButtonSprite buttonSprite = new UiComponentFactory.ButtonSprite(0, 0, 256, 256);
    
    private ModConfig config;
    
    private final List<String> crosshairs = new ArrayList<>();
    private final CrosshairService crosshairService = new CrosshairService(new CrosshairRepository());

    public SettingScreen() {
        super(FlowLayout.class, DataSource.asset(Identifier.of(CrosshairCustomizer.MOD_ID, "settings")));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        config = CrosshairCustomizer.CONFIG;
        
        CrosshairManager.setCurrentMenuType(MenuType.MAIN);

        buildTopButtons(rootComponent);
        buildCrosshairGrid(rootComponent);
        buildBottomButtons(rootComponent);
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

            ParentComponent crosshairItem = createCrosshairItem(rootComponent, crosshairNames.get(i));
            crosshairGrid.child(crosshairItem, row, col);
        }

        rootComponent.childById(FlowLayout.class, "gridContainer").child(crosshairGrid);
    }

    private void reloadCrosshairGrid(FlowLayout rootComponent) {
        FlowLayout gridContainer = rootComponent.childById(FlowLayout.class, "gridContainer");
        if (gridContainer != null) {
            new ArrayList<>(gridContainer.children()).forEach(gridContainer::removeChild);
        }
        crosshairs.clear();

        CrosshairManager.reloadCrosshairs();

        buildCrosshairGrid(rootComponent);
    }

    private FlowLayout createCrosshairItem(FlowLayout rootComponent, String crosshairName) {
        FlowLayout crosshairContainer = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(new SelectBoxComponent(8).id("ch/" + crosshairName + "/selectBox"))
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
        crosshairContainer.id("ch/" + crosshairName + "/container");
        
        if(CrosshairManager.getCurrentMenuType() == MenuType.MAIN) {
            if (this.config.currentCrosshair != null && crosshairName.equals(Identifier.of(this.config.currentCrosshair).getPath())) {
                crosshairContainer.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR).and(Surface.outline(0xD0FFFFFF)));
            }
        } else if (CrosshairManager.getCurrentMenuType() == MenuType.TARGET) {
            if (this.config.currentTargetCrosshair != null && crosshairName.equals(Identifier.of(this.config.currentTargetCrosshair).getPath())) {
                crosshairContainer.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR).and(Surface.outline(0xD0FFFFFF)));
            }
        }

        
        crosshairContainer.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (InputState.isShiftDown()) {
                SelectBoxComponent selectBox = rootComponent.childById(SelectBoxComponent.class, "ch/" + crosshairName + "/selectBox");
                if (selectBox != null) {
                    selectBox.checked(!selectBox.checked());
                }
            } else {
                switch (CrosshairManager.getCurrentMenuType()) {
                    case MAIN -> {
                        crosshairs.forEach(name -> {
                            FlowLayout item = rootComponent.childById(FlowLayout.class, "ch/" + name + "/container");
                            if (item != null) {
                                item.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR));
                            }
                        });
                        
                        if(config.currentCrosshair != null && Identifier.of(config.currentCrosshair).getPath().equals(crosshairName)) {
                            crosshairService.setCurrentCrosshair(null, MenuType.MAIN);
                            return true;
                        }
                        crosshairContainer.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR).and(Surface.outline(0xD0FFFFFF)));
                        crosshairService.setCurrentCrosshair(crosshairName, MenuType.MAIN);
                    }
                    case TARGET -> {
                        crosshairs.forEach(name -> {
                            FlowLayout item = rootComponent.childById(FlowLayout.class, "ch/" + name + "/container");
                            if (item != null) {
                                item.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR));
                            }
                        });
                        
                        if(config.currentTargetCrosshair != null && Identifier.of(config.currentTargetCrosshair).getPath().equals(crosshairName)) {
                            crosshairService.setCurrentCrosshair(null, MenuType.TARGET);
                            return true;
                        }
                        crosshairContainer.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR).and(Surface.outline(0xD0FFFFFF)));
                        crosshairService.setCurrentCrosshair(crosshairName, MenuType.TARGET);
                    }
                }
            }
            
            return true;
        });

        crosshairs.add(crosshairName);

        return crosshairContainer;
    }

    private void buildTopButtons(FlowLayout rootComponent) {
        FlowLayout topControlContainer = rootComponent.childById(FlowLayout.class, "topControlContainer");
        topControlContainer.surface(Surface.flat(0xB4101010).and(Surface.blur(1, 1)));

        topControlContainer.child(UiComponentFactory.iconButton(Identifier.of("crosshaircustomizer:textures/gui/up_order.png"),
                buttonSprite,
                Text.of("Move Crosshair Up"),
                (mouseX, mouseY, button) -> {
                    int idx = crosshairService.getSelectedIndex(CrosshairManager.getCurrentMenuType());
                    if (idx > 0) {
                        crosshairService.swapCrosshair(idx, idx - 1);
                        reloadCrosshairGrid(rootComponent);
                    }
                    
                    return true;
                })
        );

        topControlContainer.child(UiComponentFactory.iconButton(Identifier.of("crosshaircustomizer:textures/gui/down_order.png"),
                buttonSprite,
                Text.of("Move Crosshair Down"),
                (mouseX, mouseY, button) -> {
                    int idx = crosshairService.getSelectedIndex(CrosshairManager.getCurrentMenuType());
                    ModConfig config = CrosshairCustomizer.CONFIG;
                    if (idx != -1 && idx < config.crosshairs.size() - 1) {
                        crosshairService.swapCrosshair(idx, idx + 1);
                        reloadCrosshairGrid(rootComponent);
                    }
                    
                    return true;
                })
        );
        
        topControlContainer.child(Components.spacer(100).sizing(Sizing.expand(), Sizing.fixed(1)));

        topControlContainer.child(UiComponentFactory.labeledIconButton(Identifier.of("crosshaircustomizer:textures/gui/delete.png"), 
                buttonSprite,
                Text.of("Delete"),
                (mouseX, mouseY, button) -> {
                    List<String> toDelete = new ArrayList<>();
                    crosshairs.forEach(name -> {
                        SelectBoxComponent selectBox = rootComponent.childById(SelectBoxComponent.class, "ch/" + name + "/selectBox");
                        if (selectBox != null && selectBox.checked()) {
                            toDelete.add(name);
                        }
                    });

                    if (!toDelete.isEmpty()) {
                        crosshairService.deleteCrosshairs(toDelete);
                    }

                    reloadCrosshairGrid(rootComponent);
                    return true;
                })
        );
    }

    private void buildBottomButtons(FlowLayout rootComponent) {
        rootComponent.childById(FlowLayout.class, "changeMenuButton").mouseDown().subscribe((mouseX, mouseY, button) -> {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if (CrosshairManager.getCurrentMenuType() == MenuType.MAIN) {
                CrosshairManager.setCurrentMenuType(MenuType.TARGET);
                
                rootComponent.childById(FlowLayout.class, "changeMenuButton").tooltip(Text.of("Change Menu(Current: Target)"));
                
                crosshairs.forEach(name -> {
                    FlowLayout item = rootComponent.childById(FlowLayout.class, "ch/" + name + "/container");
                    if (item != null) {
                        if (this.config.currentTargetCrosshair != null && name.equals(Identifier.of(this.config.currentTargetCrosshair).getPath())) {
                            item.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR).and(Surface.outline(0xD0FFFFFF)));
                        } else {
                            item.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR));
                        }
                    }
                });
            } else if (CrosshairManager.getCurrentMenuType() == MenuType.TARGET) {
                CrosshairManager.setCurrentMenuType(MenuType.MAIN);

                rootComponent.childById(FlowLayout.class, "changeMenuButton").tooltip(Text.of("Change Menu(Current: Main)"));
                
                crosshairs.forEach(name -> {
                    FlowLayout item = rootComponent.childById(FlowLayout.class, "ch/" + name + "/container");
                    if (item != null) {
                        if (this.config.currentCrosshair != null && name.equals(Identifier.of(this.config.currentCrosshair).getPath())) {
                            item.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR).and(Surface.outline(0xD0FFFFFF)));
                        } else {
                            item.surface(Surface.flat(CROSSHAIR_CONTAINER_COLOR));
                        }
                    }
                });
            }

            crosshairs.forEach(name -> {
                SelectBoxComponent selectBox = rootComponent.childById(SelectBoxComponent.class, "ch/" + name + "/selectBox");
                if (selectBox != null) {
                    selectBox.checked(false);
                }
            });

            return true;
        });
        
        FlowLayout controlContainer = rootComponent.childById(FlowLayout.class, "controlContainer");
        controlContainer.surface(Surface.flat(0xB4101010)
                .and(Surface.blur(1, 1))
                .and(new TopOutlineSurface(0xD0666666))
        );

        controlContainer.child(UiComponentFactory.labeledIconButton(Identifier.of("crosshaircustomizer:textures/gui/add.png"),
                buttonSprite,
                Text.of("Draw"),
                (mouseX, mouseY, button) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new CanvasScreen());
                    return true;
                })
        );

        controlContainer.child(UiComponentFactory.labeledIconButton(Identifier.of("crosshaircustomizer:textures/gui/import.png"),
                buttonSprite,
                Text.of("Import"),
                (mouseX, mouseY, button) -> {
                    openImportOverlay(rootComponent);
                    return true;
                })
        );

        controlContainer.child(UiComponentFactory.labeledIconButton(Identifier.of("crosshaircustomizer:textures/gui/open_folder.png"),
                buttonSprite,
                Text.of("Open Folder"),
                (mouseX, mouseY, button) -> {
                    new Thread(() -> Util.getOperatingSystem().open(new CrosshairRepository().getCrosshairDirectoryPath())).start();
                    return true;
                })
        );
        
        controlContainer.child(Components.spacer(100).sizing(Sizing.expand(), Sizing.fixed(1)));

        controlContainer.child(UiComponentFactory.labeledIconButton(Identifier.of("crosshaircustomizer:textures/gui/reload.png"),
                buttonSprite,
                Text.of("Reload"),
                (mouseX, mouseY, button) -> {
                    reloadCrosshairGrid(rootComponent);
                    return true;
                })
        );
    }

    private void openImportOverlay(FlowLayout rootComponent) {
        CrosshairRepository crosshairRepository = new CrosshairRepository();

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

        fileNames.forEach(fileName -> dropdown.button(Text.literal(fileName), button -> {
            crosshairService.importCrosshair(fileName);

            CrosshairManager.reloadCrosshairs();

            MinecraftClient client = MinecraftClient.getInstance();
            client.setScreen(new SettingScreen());
        }));

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
