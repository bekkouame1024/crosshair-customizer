package com.bekkouame1024.mod.crosshaircustomizer.input;

public class InputState {
    private static boolean isShiftDown = false;
    private static boolean isMouseDown = false;
    private static int mouseButton = 0;

    public static boolean isShiftDown() {
        return isShiftDown;
    }
    
    public static boolean isMouseDown() {
        return isMouseDown;
    }
    
    public static int getMouseButton() {
        return mouseButton;
    }

    public static void setShiftDown(boolean shiftDown) {
        isShiftDown = shiftDown;
    }

    public static void setMouseDown(boolean mouseDown, int button) {
        isMouseDown = mouseDown;
        mouseButton = button;
    }
}
