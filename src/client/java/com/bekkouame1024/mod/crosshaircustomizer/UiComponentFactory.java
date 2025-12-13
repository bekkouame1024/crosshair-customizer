package com.bekkouame1024.mod.crosshaircustomizer;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.event.MouseDown;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class UiComponentFactory {
    public static StackLayout iconButton(Identifier textureId, ButtonSprite buttonSprite, Text tooltip, MouseDown onMouseDown) {
        StackLayout container = Containers.stack(Sizing.fixed(20), Sizing.fixed(20));
        container.margins(Insets.of(2)).surface(Surface.flat(0xD0101010)).tooltip(tooltip);
        container.horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        container.cursorStyle(CursorStyle.HAND);
        
        container.mouseDown().subscribe((mouseX, mouseY, button) -> {
            UISounds.playButtonSound();
            
            if (onMouseDown != null) {
                return onMouseDown.onMouseDown(mouseX, mouseY, button);
            }

            return false;
        });

        TextureComponent texture = Components.texture(textureId, buttonSprite.u, buttonSprite.v, buttonSprite.regionWidth, buttonSprite.regionHeight);
        texture.sizing(Sizing.fixed(16));
        texture.cursorStyle(CursorStyle.HAND);
        
        container.child(texture);

        return container;
    }
    
    public static FlowLayout labeledIconButton(Identifier textureId, ButtonSprite buttonSprite, Text text, MouseDown onMouseDown) {
        FlowLayout container = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(20));
        container.margins(Insets.of(2)).padding(Insets.horizontal(4)).surface(Surface.flat(0xD0101010));
        container.horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        container.cursorStyle(CursorStyle.HAND);
        
        container.mouseDown().subscribe((mouseX, mouseY, button) -> {
            UISounds.playButtonSound();
            
            if (onMouseDown != null) {
                return onMouseDown.onMouseDown(mouseX, mouseY, button);
            }

            return false;
        });
        
        TextureComponent icon = Components.texture(textureId, buttonSprite.u, buttonSprite.v, buttonSprite.regionWidth, buttonSprite.regionHeight);
        icon.sizing(Sizing.fixed(12)).margins(Insets.right(4));
        icon.cursorStyle(CursorStyle.HAND);

        LabelComponent label = Components.label(text);
        label.verticalTextAlignment(VerticalAlignment.CENTER);
        label.cursorStyle(CursorStyle.HAND);

        container.children(List.of(icon, label));
        

        return container;
    }

    public record ButtonSprite(int u, int v, int regionWidth, int regionHeight) {}
}
