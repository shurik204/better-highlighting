package me.shurik.betterhighlighting.util.bracket;

import me.shurik.betterhighlighting.api.syntax.BracketColorizer;

public abstract class BaseBracketColorizer implements BracketColorizer {
    protected final BracketStyles styles;

    public static BracketColorizer create(BracketStyles styles, boolean bracketIndependentColoring) {
        if (!bracketIndependentColoring) {
            return new BracketTypeAwareColorizer(styles);
        }
        return new SimpleBracketColorizer(styles);
    }

    public BaseBracketColorizer(BracketStyles styles) {
        this.styles = styles;
    }
}
