package me.shurik.betterhighlighting.api;

import org.eclipse.tm4e.core.grammar.IGrammar;

/**
 * Default grammars provided by the mod, auto-synced with the registry on reload.
 */
public final class BuiltinGrammar {
    private static IGrammar MCFUNCTION_GRAMMAR;
    private static IGrammar JSON;

    /**
     * @return mcfunction grammar
     */
    public static IGrammar mcfunction() {
        return MCFUNCTION_GRAMMAR;
    }

    /**
     * @return JSON grammar
     */
    public static IGrammar json() {
        return JSON;
    }

    static {
        TextMateResourceLoader.instance().registerReloadListener((registry -> {
            MCFUNCTION_GRAMMAR = registry.getSourceGrammar("mcfunction");
            JSON = registry.getSourceGrammar("json");
            if (MCFUNCTION_GRAMMAR == null || JSON == null) {
                throw new IllegalStateException("Failed to load built-in grammars");
            }
        }));
    }
}