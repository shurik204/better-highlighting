package me.shurik.betterhighlighting.api.syntax;

import me.shurik.betterhighlighting.api.TextMateRegistry;
import me.shurik.betterhighlighting.util.ColorUtils;
import me.shurik.betterhighlighting.util.access.StyleModifierAccess;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import org.eclipse.tm4e.core.grammar.IToken;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.internal.theme.FontStyle;
import org.eclipse.tm4e.core.internal.theme.StyleAttributes;
import org.eclipse.tm4e.core.internal.theme.Theme;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for applying formatting to tokens using TextMate themes.
 * <p>
 *   To get the current theme get the instance of {@link TextMateRegistry} and call {@link TextMateRegistry#getCurrentTheme()}.
 * </p>
 */
public final class Styler {
    private static final TextColor DEFAULT_COLOR = TextColor.fromRgb(CommonColors.WHITE);

    /**
     * Converts a list of tokens to a formatted char sequence with the given theme (for rendering in {@link EditBox})
     * @param tokenizationResult tokenization result
     * @param part string to format
     * @param theme theme to use
     * @param maxLength maximum string length
     * @return formatted char sequence
     */
    public static FormattedCharSequence tokensToCharSequence(ITokenizeLineResult<IToken[]> tokenizationResult, String part, String fullString, Theme theme, int maxLength) {
        List<FormattedCharSequence> formattedChars = new ArrayList<>();
        String paddedPart = part + " ";
        String paddedFullString = fullString + " ";
        BracketColorizer bracketColorizer = BracketColorizer.create();

        for (IToken token : tokenizationResult.getTokens()) {
            int substringStart = Math.max(token.getStartIndex() - maxLength, 0);
            int substringEnd = Math.min(token.getEndIndex() - maxLength, part.length());
            if (!(substringStart >= part.length()) && substringEnd > 0) {
                String substring = paddedPart.substring(substringStart, substringEnd);
                if (BracketColorizer.shouldConsume(token, substring)) {
                    formattedChars.add(FormattedCharSequence.forward(substring, bracketColorizer.getBracketStyle(substring)));
                } else {
                    formattedChars.add(FormattedCharSequence.forward(substring, getMinecraftStyle(token, theme)));
                }
            } else {
                // Ugly workaround to fix incorrect bracket coloring
                String substring = paddedFullString.substring(token.getStartIndex(), token.getEndIndex());
                if (BracketColorizer.shouldConsume(token, substring)) {
                    bracketColorizer.getBracketStyle(substring);
                }
            }
        }
        return FormattedCharSequence.composite(formattedChars);
    }

    /**
     * Converts a list of tokens to a formatted char sequence
     *
     * @param tokenizationResult tokenization result
     * @param string             string to format
     * @param theme              theme to use
     * @return formatted char sequence
     */
    public static FormattedCharSequence tokensToCharSequence(ITokenizeLineResult<IToken[]> tokenizationResult, String string, Theme theme) {
        List<FormattedCharSequence> formattedChars = new ArrayList<>();
        BracketColorizer bracketColorizer = BracketColorizer.create();
        // For some reason, having a trailing space makes TM4E set the end index of the last token outside the string
        String padded = string + " ";

        for (IToken token : tokenizationResult.getTokens()) {
            formattedChars.add(Styler.formatTokenAsCharSequence(token, padded, theme, bracketColorizer));
        }

        return FormattedCharSequence.composite(formattedChars);
    }

    /**
     * Converts a list of tokens to a list of components
     * @param tokenizationResult tokenization result
     * @param string string to format
     * @return list of components
     */
    public static List<Component> tokensToComponents(ITokenizeLineResult<IToken[]> tokenizationResult, String string, Theme theme) {
        List<Component> components = new ArrayList<>();
        BracketColorizer bracketColorizer = BracketColorizer.create();
        // For some reason, having a trailing space makes TM4E set the end index of the last token outside the string
        String padded = string + " ";

        for (IToken token : tokenizationResult.getTokens()) {
            components.add(Styler.formatTokenAsComponent(token, padded, theme, bracketColorizer));
        }

        return components;
    }

    /**
     * Formats a token as a char sequence with the given theme
     * @param token token to format
     * @param string string to format
     * @param theme theme to use
     * @return formatted char sequence
     */
    public static FormattedCharSequence formatTokenAsCharSequence(IToken token, String string, Theme theme, @Nullable BracketColorizer bracketColorizer) {
        String substring = string.substring(token.getStartIndex(), token.getEndIndex());
        if (bracketColorizer != null && BracketColorizer.shouldConsume(token, substring)) {
            return FormattedCharSequence.forward(substring, bracketColorizer.getBracketStyle(substring));
        }
        return FormattedCharSequence.forward(substring, getMinecraftStyle(token, theme));
    }

    /**
     * Formats a token as a component with the given theme
     * @param token token to format
     * @param string string to format
     * @param theme theme to use
     * @return formatted component
     */
    public static MutableComponent formatTokenAsComponent(IToken token, String string, Theme theme, @Nullable BracketColorizer bracketColorizer) {
        String substring = string.substring(token.getStartIndex(), token.getEndIndex());
        if (bracketColorizer != null && BracketColorizer.shouldConsume(token, substring)) {
            return Component.literal(substring).setStyle(bracketColorizer.getBracketStyle(substring));
        }
        return Component.literal(substring).setStyle(getMinecraftStyle(token, theme));
    }

    /**
     * Gets the TextMate style for the given token
     * @param token token to get style for
     * @return style attributes
     */
    public static StyleAttributes getTextMateStyle(IToken token) {
        return getTextMateStyle(token, TextMateRegistry.instance().getCurrentTheme());
    }

    /**
     * Gets the TextMate style for the given token with the given theme
     * @param token token to get style for
     * @param theme theme to use
     * @return style attributes
     */
    public static StyleAttributes getTextMateStyle(IToken token, Theme theme) {
        return Objects.requireNonNullElseGet(
                theme.match(Tokenizer.getScopeStack(token)),
                theme::getDefaults
        );
    }

    /**
     * Gets the Minecraft style for the given token
     * @implNote if you need bracket coloring, use {@link #formatTokenAsComponent(IToken, String, Theme, BracketColorizer)}
     * @param token token to get style for
     * @return Minecraft style
     */
    public static Style getMinecraftStyle(IToken token) {
        return toMinecraftStyle(getTextMateStyle(token), TextMateRegistry.instance().getCurrentTheme());
    }

    /**
     * Gets the Minecraft style for the given token with the given theme
     * @param token token to get style for
     * @param theme theme to use
     * @return Minecraft style
     */
    public static Style getMinecraftStyle(IToken token, Theme theme) {
        return toMinecraftStyle(getTextMateStyle(token), theme);
    }

    /**
     * Converts a TextMate style to a Minecraft style
     * @param attributes style attributes
     * @return Minecraft style
     */
    public static Style toMinecraftStyle(StyleAttributes attributes, Theme theme) {
        Style style = Style.EMPTY.withColor(getTextColor(theme, attributes.foregroundId));
        if (attributes.fontStyle == FontStyle.NotSet) {
            return style;
        }
        return ((StyleModifierAccess)style).highlight$updateModifiers(
                FontStyle.isBold(attributes.fontStyle),
                FontStyle.isItalic(attributes.fontStyle),
                FontStyle.isUnderline(attributes.fontStyle),
                FontStyle.isStrikethrough(attributes.fontStyle)
        );
    }

    /**
     * Gets the color string for the given ID
     * @param theme theme to get color from
     * @param id color ID
     * @return color string
     */
    public static String getColorString(Theme theme, int id) {
        try {
            return theme.getColorMap().get(id);
        } catch (IndexOutOfBoundsException e) {
            return "#FFFFFF";
        }
    }

    /**
     * Gets the text color for the given ID
     * @param theme theme to get color from
     * @param id color ID
     * @return text color
     */
    public static TextColor getTextColor(Theme theme, int id) {
        TextColor result = ColorUtils.parseColor(getColorString(theme, id));
        return result != null ? result : DEFAULT_COLOR;
    }
}