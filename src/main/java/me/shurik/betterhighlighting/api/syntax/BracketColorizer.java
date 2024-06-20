package me.shurik.betterhighlighting.api.syntax;

import me.shurik.betterhighlighting.Config;
import me.shurik.betterhighlighting.api.TextMateRegistry;
import me.shurik.betterhighlighting.util.bracket.BaseBracketColorizer;
import net.minecraft.network.chat.Style;
import org.eclipse.tm4e.core.grammar.IToken;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Bracket pair colorizer.
 * <p>
 *   Default implementation uses a single index for all bracket types to determine the color.
 * </p>
 * @see me.shurik.betterhighlighting.util.bracket.SimpleBracketColorizer
 * @see me.shurik.betterhighlighting.util.bracket.BracketTypeAwareColorizer
 */
public interface BracketColorizer {
    /**
     * Create a new bracket colorizer.
     * @return bracket colorizer
     */
    static BracketColorizer create() {
        return BaseBracketColorizer.create(TextMateRegistry.instance().getBracketStyles(), Config.INSTANCE.bracketIndependentColoring);
    }

    /**
     * Get the style for the given bracket.
     * @param bracket bracket
     * @return minecraft text style
     */
    default Style getBracketStyle(String bracket) {
        return getBracketStyle(BracketType.fromChar(bracket.charAt(0)));
    }

    /**
     * Get the style for the given bracket type.
     * @param bracketType bracket type
     * @return minecraft text style
     */
    Style getBracketStyle(BracketType bracketType);

    /**
     * Check if the token should be consumed by the bracket colorizer.
     * @param token token
     * @param bracket bracket
     * @return true if the token should be passed to {@link #getBracketStyle}
     */
    static boolean shouldConsume(IToken token, String bracket) {
        //                                         /                🩼             / replacing with getLast() screws up compilation fsr
        if (bracket.isBlank() || token.getScopes().get(token.getScopes().size() - 1).startsWith("string")) {
            return false;
        }
        // Return true if the string either has a length of 1 and is a bracket
        if (bracket.length() == 1 && BracketType.fromChar(bracket.charAt(0)) != null) {
            return true;
        }
        // or only contains bracket characters
        BracketType type = BracketType.fromString(bracket.substring(0, 1));
        if (type == null) {
            return false;
        }
        return bracket.chars().allMatch(c -> c == ')' || c == ']' || c == '}');
    }

    /**
     * Bracket type.
     */
    enum BracketType {
        PARENTHESIS(0, '(', false),
        CLOSING_PARENTHESIS(0, ')', true),
        SQUARE_BRACKET(1,'[', false),
        CLOSING_SQUARE_BRACKET(1, ']', true),
        CURLY_BRACKET(2, '{', false),
        CLOSING_CURLY_BRACKET(2, '}', true);

        public final int index;
        public final char character;
        public final boolean closing;

        BracketType(int index, char character, boolean closing) {
            this.index = index;
            this.character = character;
            this.closing = closing;
        }

        public boolean matchesClosing(BracketType other) {
            return this.index == other.index && this.closing != other.closing;
        }

        /**
         * Get the bracket type from a string.
         * @param s one-character string
         * @return bracket type, or null if the string is not a bracket
         */
        @Nullable
        static BracketType fromString(String s) {
            if (s.length() != 1) {
                return null;
            }
            return fromChar(s.charAt(0));
        }

        /**
         * Get the bracket type from a character.
         * @param c character
         * @return bracket type, or null if the character is not a bracket
         */
        @Nullable
        static BracketType fromChar(char c) {
            return switch (c) {
                case '(' -> PARENTHESIS;
                case ')' -> CLOSING_PARENTHESIS;
                case '[' -> SQUARE_BRACKET;
                case ']' -> CLOSING_SQUARE_BRACKET;
                case '{' -> CURLY_BRACKET;
                case '}' -> CLOSING_CURLY_BRACKET;
                default -> null;
            };
        }
    }

    /**
     * Bracket styles holder.
     * <p>
     *     To get currently used bracket styles, use {@link TextMateRegistry#getBracketStyles()}.
     * </p>
     */
    record BracketStyles(List<Style> styles, Style unmatchedBracketStyle) {
        public Style getStyle(int index) {
            if (index < 0) {
                return unmatchedBracketStyle;
            }
            return styles.get(index % styles.size());
        }

        /**
         * Create a new bracket styles holder from a list of colors.
         * @param bracketStyles list of colors
         * @param unmatchedBracketColor color for unmatched brackets
         * @return bracket styles holder
         */
        public static BracketStyles fromColors(List<Integer> bracketStyles, int unmatchedBracketColor) {
            return new BracketStyles(
                bracketStyles.stream().map(Style.EMPTY::withColor).toList(),
                Style.EMPTY.withColor(unmatchedBracketColor)
            );
        }
    }
}