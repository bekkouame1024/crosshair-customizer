package com.bekkouame1024.mod.crosshaircustomizer.screen;

import com.bekkouame1024.mod.crosshaircustomizer.*;
import com.bekkouame1024.mod.crosshaircustomizer.input.InputState;
import com.bekkouame1024.mod.crosshaircustomizer.model.CanvasModel;
import com.bekkouame1024.mod.crosshaircustomizer.utils.ColorParsers;
import com.bekkouame1024.mod.crosshaircustomizer.repository.CrosshairRepository;
import com.bekkouame1024.mod.crosshaircustomizer.service.CrosshairService;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;

public class CanvasScreen extends BaseUIModelScreen<FlowLayout> {
    private static final int CELL_SIZE = 14;
    private static final int GRID_COLUMNS = 15;
    private static final int GRID_ROWS = 15;
    private static final int MARGIN_SIZE = 1;
    private static final UiComponentFactory.ButtonSprite buttonSprite = new UiComponentFactory.ButtonSprite(0, 0, 256, 256);
    
    private final CanvasModel canvasModel = new CanvasModel(GRID_COLUMNS, GRID_ROWS);

    public CanvasScreen() {
        super(FlowLayout.class, DataSource.asset(Identifier.of(CrosshairCustomizer.MOD_ID, "canvas")));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        buildTopButtons(rootComponent);
        
        FlowLayout colorPickerContainer = rootComponent.childById(FlowLayout.class, "colorPickerContainer").gap(8);
        
        FlowLayout currentColorContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(50)).gap(4);
        
        BoxComponent currentColorComponent = Components.box(Sizing.fill(100), Sizing.fill(85)).fill(true);

        FlowLayout textBoxContainer = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        textBoxContainer.surface(new BottomOutlineSurface(0xD0ffffff));
        
        TextBoxComponent textBox = Components.textBox(Sizing.fill(), "");
        textBox.setPlaceholder(Text.literal("Hex Color Code... (#AARRGGBB)").formatted(Formatting.GRAY));
        textBox.setDrawsBackground(false);
        textBox.setMaxLength("#AARRGGBB".length());

        textBoxContainer.child(textBox);

        ColorPickerComponent colorPicker = new ColorPickerComponent().showAlpha(true);
        colorPicker.sizing(Sizing.fill(100), Sizing.fill(46));
        colorPicker.onChanged().subscribe(color -> {
            currentColorComponent.color(color);
            textBox.setText(color.asHexString(true));
        });
        
        textBox.onChanged().subscribe(text -> {
            Color color = ColorParsers.tryParse(text);
            if(color == null) {
                return;
            }
            
            colorPicker.selectedColor(color);
        });
        
        currentColorContainer.child(textBoxContainer);
        currentColorContainer.child(0, currentColorComponent);
        

        colorPickerContainer.child(colorPicker);
        colorPickerContainer.child(currentColorContainer);
        
        rootComponent.childById(FlowLayout.class, "mainContainer")
                .surface(Surface.blur(1f, 1f).and(Surface.flat(0xB4101010)).and(new TopOutlineSurface(0xD0666666)));

        GridLayout canvasGrid = Containers.grid(Sizing.content(), Sizing.content(), GRID_ROWS, GRID_COLUMNS);
        canvasGrid.surface(Surface.flat(0xB4666666))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        for (int i = 0; i < GRID_COLUMNS * GRID_ROWS; i++) {
            int row = i / GRID_COLUMNS;
            int col = i % GRID_COLUMNS;

            BoxComponent cellBox = Components
                    .box(Sizing.fixed(CELL_SIZE - MARGIN_SIZE), Sizing.fixed(CELL_SIZE - MARGIN_SIZE))
                    .color(Color.ofArgb(0xB4808080))
                    .fill(true);

            int right = (col < GRID_COLUMNS - 1) ? MARGIN_SIZE : 0;
            int bottom = (row < GRID_ROWS - 1) ? MARGIN_SIZE : 0;
            cellBox.margins(Insets.of(0, bottom, 0, right));

            cellBox.mouseEnter().subscribe(() -> {
                if (!InputState.isMouseDown()) {
                    return;
                }
                
                switch (InputState.getMouseButton()) {
                    case 0 -> {
                        cellBox.color(colorPicker.selectedColor());
                        canvasModel.setColor(col, row, colorPicker.selectedColor());
                    }
                    case 1 -> {
                        cellBox.color(Color.ofArgb(0xB4808080));
                        canvasModel.setColor(col, row, Color.ofArgb(0x00000000));
                    }
                    case 2 -> {
                        Color currentColor = canvasModel.getColor(col, row);
                        colorPicker.selectedColor(currentColor);
                    }
                }
            });

            cellBox.mouseDown().subscribe((mouseX, mouseY, button) -> {
                switch (button) {
                    case 0 -> {
                        cellBox.color(colorPicker.selectedColor());
                        canvasModel.setColor(col, row, colorPicker.selectedColor());
                    }
                    case 1 -> {
                        cellBox.color(Color.ofArgb(0xB4808080));
                        canvasModel.setColor(col, row, Color.ofArgb(0x00000000));
                    }
                    case 2 -> {
                        Color currentColor = canvasModel.getColor(col, row);
                        colorPicker.selectedColor(currentColor);
                    }
                }

                return true;
            });

            canvasGrid.child(cellBox, row, col);
        }

        rootComponent.childById(FlowLayout.class, "gridContainer").child(canvasGrid);

        buildBottomButtons(rootComponent);
    }
    
    private void buildTopButtons(FlowLayout rootComponent) {
        rootComponent.childById(FlowLayout.class, "topControlContainer")
                .surface(Surface.blur(1f, 1f).and(Surface.flat(0xB4101010)));
    }

    private void buildBottomButtons(FlowLayout rootComponent) {
        FlowLayout controlContainer = rootComponent.childById(FlowLayout.class, "controlContainer");
        controlContainer.surface(Surface.blur(1f, 1f).and(Surface.flat(0xB4101010)).and(new TopOutlineSurface(0xD0666666)));

        controlContainer.child(UiComponentFactory.labeledIconButton(Identifier.of("crosshaircustomizer:textures/gui/save.png"),
                buttonSprite,
                Text.of("Save"),
                (mouseX, mouseY, button) -> {
                    openSaveOverlay(rootComponent);
                    return true;
                })
        );
        
        controlContainer.child(UiComponentFactory.labeledIconButton(Identifier.of("crosshaircustomizer:textures/gui/cancel.png"),
                buttonSprite,
                Text.of("Cancel"),
                (mouseX, mouseY, button) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new SettingScreen());
                    return true;
                })
        );
    }

    private void saveImage(String name) {
        CrosshairRepository repository = new CrosshairRepository();
        CrosshairService service = new CrosshairService(repository);

        BufferedImage bufferedImage = canvasModel.toBufferedImage();
        CrosshairService.SaveResult result = service.saveCrosshair(name, bufferedImage);

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (!result.isOk()) {
            if (player != null) {
                player.sendMessage(Text.literal(result.getMessage()).formatted(Formatting.RED), true);
            }
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new SettingScreen());
    }

    private void openSaveOverlay(FlowLayout rootComponent) {
        FlowLayout saveContainer = Containers.verticalFlow(Sizing.fixed(CELL_SIZE * GRID_COLUMNS + 10), Sizing.fixed(80));
        saveContainer
                .surface(Surface.blur(1f, 1f).and(Surface.flat(0xD4101010).and(Surface.outline(0xD0666666))))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER).zIndex(5);

        OverlayContainer<FlowLayout> overlay = Containers.overlay(saveContainer);
        overlay.surface(Surface.BLANK);

        FlowLayout textBoxContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        textBoxContainer.surface(new BottomOutlineSurface(0xD0ffffff));

        TextBoxComponent textBox = Components.textBox(Sizing.fixed(CELL_SIZE * GRID_COLUMNS - 10), "");
        textBox.setPlaceholder(Text.literal("Enter crosshair name...").formatted(Formatting.GRAY));
        textBox.setDrawsBackground(false);

        textBoxContainer.child(textBox);

        ButtonComponent okButton = Components.button(Text.literal("OK"), button -> saveImage(textBox.getText()));
        okButton.textShadow(false)
                .renderer(ButtonComponent.Renderer.flat(0xD0101010, 0xD0101010, 0xD0101010))
                .margins(Insets.of(10, 0, 0, 0))
                .sizing(Sizing.fixed(50), Sizing.content());

        saveContainer
                .child(textBoxContainer)
                .child(okButton);
        rootComponent.child(overlay);
    }
}
