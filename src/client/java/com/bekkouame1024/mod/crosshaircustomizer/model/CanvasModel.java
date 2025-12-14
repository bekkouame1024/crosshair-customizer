package com.bekkouame1024.mod.crosshaircustomizer.model;

import io.wispforest.owo.ui.core.Color;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class CanvasModel {
    private final int columns;
    private final int rows;
    private final Color[][] colors;

    public CanvasModel(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        this.colors = new Color[rows][columns];
        for (int r = 0; r < rows; r++) {
            Arrays.fill(this.colors[r], Color.ofArgb(0x00000000));
        }
    }

    public int getColumns() { return columns; }
    public int getRows() { return rows; }

    public Color getColor(int column, int row) {
        if (row < 0 || row >= rows || column < 0 || column >= columns) return Color.ofArgb(0x00000000);
        Color c = colors[row][column];
        return c == null ? Color.ofArgb(0x00000000) : c;
    }

    public void setColor(int column, int row, Color color) {
        if (row < 0 || row >= rows || column < 0 || column >= columns) return;
        colors[row][column] = color == null ? Color.ofArgb(0x00000000) : color;
    }

    public BufferedImage toBufferedImage() {
        BufferedImage bufferedImage = new BufferedImage(columns, rows, BufferedImage.TYPE_INT_ARGB);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                Color color = getColor(c, r);
                bufferedImage.setRGB(c, r, color.argb());
            }
        }
        return bufferedImage;
    }

    public void clear() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                colors[r][c] = Color.ofArgb(0x00000000);
            }
        }
    }
}

