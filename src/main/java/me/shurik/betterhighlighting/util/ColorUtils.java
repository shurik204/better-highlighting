package me.shurik.betterhighlighting.util;

import me.shurik.betterhighlighting.mixin.TextColorAccessor;
import net.minecraft.Util;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ColorUtils {
    private static final Function<String, TextColor> STRING_TO_TEXT_COLOR = Util.memoize(ColorUtils::parseColor);
    private static final Function<Integer, Style> COLOR_TO_STYLE = Util.memoize(Style.EMPTY::withColor);

    public static TextColor textColorFromString(String value) {
        return STRING_TO_TEXT_COLOR.apply(value);
    }

    public static int colorFromString(String value) {
        return textColorFromString(value).getValue();
    }

    // TODO: remove
    public static Style styleFromColor(int color) {
        return COLOR_TO_STYLE.apply(color);
    }

    public static double luminance(int color) {
        double r = (color >> 16 & 255) / 255.0D;
        double g = (color >> 8 & 255) / 255.0D;
        double b = (color & 255) / 255.0D;
        return 0.2126D * r + 0.7152D * g + 0.0722D * b;
    }

    @Nullable
    public static TextColor parseColor(String value) {
        if (value.startsWith("#")) {
            try {
                int color = Integer.parseInt(value.substring(1), 16);
                return TextColor.fromRgb(color);
            } catch (NumberFormatException var2) {
                return null;
            }
        } else {
            return TextColorAccessor.getNamedColors().get(value);
        }
    }
}