package com.bekkouame1024.mod.crosshaircustomizer.config;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    public int version = 2;
    public String currentCrosshair = null;
    public String currentTargetCrosshair = null;
    public List<CrosshairEntry> crosshairs = new ArrayList<>();

    public static class CrosshairEntry {
        public String file;
        public int order;

        public CrosshairEntry() {}

        public CrosshairEntry(String file, int order) {
            this.file = file;
            this.order = order;
        }
    }
}
