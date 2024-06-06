package me.shurik.betterhighlighting.api;

import me.shurik.betterhighlighting.api.syntax.BracketColorizer;
import me.shurik.betterhighlighting.mixin.tm4e.SyncRegistryAccessor;
import me.shurik.betterhighlighting.resource.TextMateRegistryImpl;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.registry.SyncRegistry;
import org.eclipse.tm4e.core.internal.theme.Theme;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.IThemeSource;
import org.eclipse.tm4e.core.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Main TextMate registry to manage loaded grammars and themes.
 * <p>
 * Acts as a wrapper for the TM4E {@link Registry}.
 * </p>
 */
public interface TextMateRegistry {
    /**
     * Get current registry instance
     * @return registry
     */
    static TextMateRegistry instance() {
        return TextMateRegistryImpl.INSTANCE;
    }

    /**
     * Get the source grammar for the given language.
     * @param language language identifier (json, mcfunction, etc.)
     * @implNote if you're looking for mcfunction or JSON grammar, check out {@link BuiltinGrammar}.
     * @return grammar for the given language source
     */
    @Nullable
    default IGrammar getSourceGrammar(String language) {
        return getGrammar("source." + language);
    }

    /**
     * Get the grammar for the given language
     * @implNote source code grammar uses {@code source.} prefix so most of the time you want to use {@link #getSourceGrammar(String)} instead
     * @param scope grammar scope
     * @return grammar for the given language
     */
    @Nullable
    IGrammar getGrammar(String scope);

    /**
     * Get grammar list
     * @return grammar scopes
     */
    Set<String> getGrammarList();

    /**
     * Register a new grammar from the given source
     * @param source grammar source
     * @return registered grammar
     */
    IGrammar registerGrammar(IGrammarSource source);

    /**
     * Get the theme with the given name
     * @param name theme name
     * @return theme with the given name
     */
    @Nullable
    Theme getTheme(String name);

    /**
     * Get theme list
     * @return theme names
     */
    Set<String> getThemeList();

    /**
     * Register a new theme from the given source
     * @param source theme source
     * @return registered theme
     */
    Theme registerTheme(IThemeSource source);

    /**
     * Set the current theme by name
     * @param name theme name
     */
    boolean setTheme(String name);

    /**
     * Get the current theme
     * @return theme
     */
    Theme getCurrentTheme();

    /**
     * Get the current theme name
     * @return theme name
     */
    String getCurrentThemeName();

    // Registry operations //

    /**
     * Get the underlying registry
     * @return registry
     */
    Registry getTMRegistry();

    /**
     * Get the "sync registry"
     * @return sync registry
     */
    SyncRegistry getSyncRegistry();

    /**
     * Get the "sync registry" accessor
     * Used to get the current theme and grammar list
     * @return sync registry accessor
     */
    SyncRegistryAccessor getSyncRegistryAccessor();

    /**
     * Reset the registry.
     */
    void reset();

    /**
     * Set the bracket colorizer styles
     * @param styles bracket colorizer styles
     */
    void setBracketStyles(BracketColorizer.BracketStyles styles);

    /**
     * Get the bracket colorizer styles
     * @return bracket colorizer styles
     */
    BracketColorizer.BracketStyles getBracketStyles();
}