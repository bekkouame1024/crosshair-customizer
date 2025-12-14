package com.bekkouame1024.mod.crosshaircustomizer.utils;

import io.wispforest.owo.ui.core.Color;

public class ColorParsers {
    public static Color tryParse(String text) {
        if (text.matches("#([A-Fa-f\\d]{2}){4}")) {
            return Color.ofArgb(Integer.parseUnsignedInt(text.substring(1), 16));
        } else {
            // 入力途中ではパースに失敗する可能性があるため、その場合は処理を行わない。
            // 入力完了後にパースが失敗しても、メインの動作には影響しないためエラー表示は行わない。
            return null;
        }
    }
}
