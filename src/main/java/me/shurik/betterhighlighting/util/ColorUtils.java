package me.shurik.betterhighlighting.util;

import net.minecraft.Util;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.function.Function;

public class ColorUtils {
    private static final Function<String, TextColor> HEX_TO_TEXT_COLOR = Util.memoize((hex) -> TextColor.parseColor(hex).getOrThrow(false, (error) -> {}));
    private static final Function<Integer, Style> COLOR_TO_STYLE = Util.memoize(Style.EMPTY::withColor);

    public static TextColor textColorFromHex(String hex) {
        return HEX_TO_TEXT_COLOR.apply(hex);
    }

    public static int colorFromHex(String hex) {
        return textColorFromHex(hex).getValue();
    }

    public static Style styleFromColor(int color) {
        return COLOR_TO_STYLE.apply(color);
    }

    public static double luminance(int color) {
        double r = (color >> 16 & 255) / 255.0D;
        double g = (color >> 8 & 255) / 255.0D;
        double b = (color & 255) / 255.0D;
        return 0.2126D * r + 0.7152D * g + 0.0722D * b;
    }
}
