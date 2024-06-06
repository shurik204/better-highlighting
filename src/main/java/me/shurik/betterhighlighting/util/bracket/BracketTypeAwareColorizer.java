package me.shurik.betterhighlighting.util.bracket;

import me.shurik.betterhighlighting.api.syntax.BracketColorizer;
import net.minecraft.network.chat.Style;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link BracketColorizer} that tracks color indices for each bracket type separately.
 */
public class BracketTypeAwareColorizer extends BaseBracketColorizer {
    private final AtomicInteger[] indices = new AtomicInteger[] {new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0)};

    public BracketTypeAwareColorizer(BracketStyles styles) {
        super(styles);
    }

    @Override
    public Style getBracketStyle(BracketType bracketType) {
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
