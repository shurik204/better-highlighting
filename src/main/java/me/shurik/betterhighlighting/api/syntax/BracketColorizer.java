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
 * Default implementation uses a single index for all bracket types to determine the color.
 * @see me.shurik.betterhighlighting.util.bracket.SimpleBracketColorizer
 * @see me.shurik.betterhighlighting.util.bracket.BracketTypeAwareColorizer
 */
public interface BracketColorizer {
    static BracketColorizer create() {
        return BaseBracketColorizer.create(TextMateRegistry.instance().getBracketStyles(), Config.INSTANCE.bracketIndependentColoring);
    }

    default Style getBracketStyle(String bracket) {
        return getBracketStyle(BracketType.fromChar(bracket.charAt(0)));
    }

    Style getBracketStyle(BracketType bracketType);

    static boolean shouldConsume(IToken token, String bracket) {
        //                                         /                🩼             / replacing with getLast() screws up compilation fsr
        if (bracket.isBlank() || token.getScopes().get(token.getScopes().size() - 1).startsWith("string")) {
            return false;
        }
        // Return true if the string either has a length of 1 and is a bracket
        if (bracket.length() == 1 && BracketType.fromChar(bracket.charAt(0)) != null) {
            return true;
        }
        // or only contains bracket characters of the same type
        BracketType type = BracketType.fromString(bracket.substring(0, 1));
        if (type == null) {
            return false;
        }
        return bracket.chars().allMatch(c -> c == ')' || c == ']' || c == '}');
    }

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

        @Nullable
        static BracketType fromString(String s) {
            if (s.length() != 1) {
                return null;
            }
            return fromChar(s.charAt(0));
        }

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

    record BracketStyles(List<Style> styles, Style unmatchedBracketStyle) {
        public Style getStyle(int index) {
            if (index < 0) {
                return unmatchedBracketStyle;
            }
            return styles.get(index % styles.size());
        }

        public static BracketStyles fromColors(List<Integer> bracketStyles, int unmatchedBracketColor) {
            return new BracketStyles(
                bracketStyles.stream().map(Style.EMPTY::withColor).toList(),
                Style.EMPTY.withColor(unmatchedBracketColor)
            );
        }
    }
}