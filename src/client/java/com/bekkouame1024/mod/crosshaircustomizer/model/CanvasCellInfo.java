package com.bekkouame1024.mod.crosshaircustomizer.model;

import io.wispforest.owo.ui.core.Color;

public class CanvasCellInfo {
    private final int column;
    private final int row;
    private Color color = Color.ofArgb(0x00000000);
    
    public CanvasCellInfo(int column, int row) {
        this.column = column;
        this.row = row;
    }
    
    public int getColumn() {
        return column;
    }
    
    public int getRow() {
        return row;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
}
