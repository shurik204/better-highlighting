package me.shurik.betterhighlighting.util.bracket;

import net.minecraft.network.chat.Style;

import java.util.Stack;

public class SimpleBracketColorizer extends BaseBracketColorizer {
    private final Stack<BracketType> brackets = new Stack<>();

    public SimpleBracketColorizer(BracketStyles styles) {
        super(styles);
    }

    @Override
    public Style getBracketStyle(BracketType bracketType) {
        if (bracketType.closing) {
            // If we get a closing bracket and there are no opening brackets, we have an unmatched bracket
            if (brackets.isEmpty()) {
                return styles.unmatchedBracketStyle();
            }
            // If the closing bracket matches the last opening bracket, we remove it
            if (bracketType.matchesClosing(brackets.peek())) {
                int index = brackets.size() - 1;
                brackets.pop();
                return styles.getStyle(index);
            } else {
                // If the closing bracket does not match the last opening bracket, we have an unmatched bracket
                return styles.unmatchedBracketStyle();
            }
        }
        // Opening bracket, add it to the stack and return the style
        brackets.push(bracketType);
        return styles.getStyle(brackets.size() - 1);
    }
}
