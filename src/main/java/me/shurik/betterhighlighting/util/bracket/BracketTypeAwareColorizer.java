package me.shurik.betterhighlighting.util.bracket;

import me.shurik.betterhighlighting.api.syntax.BracketColorizer;
import net.minecraft.network.chat.Style;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link BracketColorizer} that tracks color indices for each bracket type separately.
 */
public class BracketTypeAwareColorizer extends BaseBracketColorizer {
    private final Stack<BracketType> brackets = new Stack<>();
    private final AtomicInteger[] indices = new AtomicInteger[] {new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0)};

    public BracketTypeAwareColorizer(BracketStyles styles) {
        super(styles);
    }

    @Override
    public Style getBracketStyle(BracketType bracketType) {
        if (bracketType.closing) {
            // If we get a closing bracket and there are no opening brackets, we have an unmatched bracket
            if (brackets.isEmpty()) {
                return styles.unmatchedBracketStyle();
            }
            // If the closing bracket matches the last opening bracket, pop it from the stack and return the matching style
            if (bracketType.matchesClosing(brackets.peek())) {
                int index = modifyIndex(bracketType, indices[bracketType.index]);
                brackets.pop();
                return styles.getStyle(index);
            } else {
                // If the closing bracket does not match the last opening bracket, we have an unmatched bracket
                return styles.unmatchedBracketStyle();
            }
        }
        // Opening bracket, push it into the stack and return the style
        brackets.push(bracketType);
        return styles.getStyle(modifyIndex(bracketType, indices[bracketType.index]));
    }

    private int modifyIndex(BracketType bracket, AtomicInteger index) {
        if (bracket.closing) {
            if (index.get() <= 0) {
                return -1;
            }
            return index.decrementAndGet();
        } else {
            return index.getAndIncrement();
        }
    }
}
