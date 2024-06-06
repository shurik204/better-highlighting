package me.shurik.betterhighlighting.resource;

import me.shurik.betterhighlighting.api.syntax.BracketColorizer;
import me.shurik.betterhighlighting.api.TextMateRegistry;
import me.shurik.betterhighlighting.mixin.tm4e.RegistryAccessor;
import me.shurik.betterhighlighting.mixin.tm4e.SyncRegistryAccessor;
import org.eclipse.tm4e.core.TMException;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.registry.SyncRegistry;
import org.eclipse.tm4e.core.internal.theme.Theme;
import org.eclipse.tm4e.core.internal.theme.raw.IRawTheme;
import org.eclipse.tm4e.core.internal.theme.raw.RawThemeReader;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.IThemeSource;
import org.eclipse.tm4e.core.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static me.shurik.betterhighlighting.BetterHighlighting.LOGGER;

public class TextMateRegistryImpl implements TextMateRegistry {
    public static final TextMateRegistry INSTANCE = new TextMateRegistryImpl();

    /** Scope name -> grammar source file path */
    private final Map<String, String> grammarSources = new HashMap<>();
    /** Theme name -> theme source file path */
    private final Map<String, String> themeSources = new HashMap<>();
    /** TM4E registry */
    private Registry registry;
    /** Sync registry pulled from {@code registry} */
    private SyncRegistry syncRegistry;
    /** Sync registry accessor */
    private SyncRegistryAccessor syncRegistryAccessor;
    private final Map<String, Theme> themes = new HashMap<>();
    private String currentThemeName = "";
    private BracketColorizer.BracketStyles bracketStyles;

    private TextMateRegistryImpl() {
        createRegistry();
    }

    private void createRegistry() {
        registry = new Registry();
        syncRegistry = ((RegistryAccessor) (Object) registry).getSyncRegistry();
        syncRegistryAccessor = (SyncRegistryAccessor) syncRegistry;
    }

    @Override
    public void reset() {
        createRegistry();

        grammarSources.clear();
        themeSources.clear();
        themes.clear();
        currentThemeName = "";
    }

    @Override
    public void setBracketStyles(BracketColorizer.BracketStyles styles) {
        bracketStyles = styles;
    }

    @Override
    public BracketColorizer.BracketStyles getBracketStyles() {
        return bracketStyles;
    }

    @Override
    @Nullable
    public IGrammar getGrammar(String language) {
        return registry.grammarForScopeName(language);
    }

    @Override
    public Set<String> getGrammarList() {
        return getSyncRegistryAccessor().getGrammars().keySet();
    }

    @Override
    public IGrammar registerGrammar(IGrammarSource source) {
        IGrammar grammar = tryRegisterGrammar(source);
        if (grammar == null) {
            return null;
        }
        String overwritten = grammarSources.put(grammar.getScopeName(), source.getFilePath());
        if (overwritten != null) {
            LOGGER.warn("Grammar for '{}' loaded from '{}' was overwritten by '{}'!", grammar.getScopeName(), overwritten, source.getFilePath());
        }
//        else {
//            LOGGER.info("Registered grammar for '{}' from '{}'", grammar.getScopeName(), source.getFilePath());
//        }
        return grammar;
    }

    private @Nullable IGrammar tryRegisterGrammar(IGrammarSource source) {
        try {
            return registry.addGrammar(source);
        } catch (TMException e) {
            LOGGER.error("Failed to register grammar from '{}'", source.getFilePath(), e);
            //            Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).forEach(BetterHighlighting.LOGGER::error);
        }
        return null;
    }

    @Override
    public Theme registerTheme(IThemeSource source) {
        IRawTheme rawTheme = tryLoadTheme(source);
        if (rawTheme == null) {
            // Failed to load the theme
            return null;
        }
        // Grab the theme name
        String name = rawTheme.getName();
        if (name == null) {
            // If the theme has no name, use the file path
            LOGGER.warn("Theme '{}' has no 'name' field!", source.getFilePath());
            name = source.getFilePath();
        }
        // Actually create and register the theme
        Theme theme = Theme.createFromRawTheme(rawTheme, null);
        themes.put(name, theme);
        // Overwrite warning
        String overwritten = themeSources.put(name, source.getFilePath());
        if (overwritten != null) {
            LOGGER.warn("Theme '{}' loaded from '{}' was overwritten by '{}'!", name, overwritten, source.getFilePath());
        }
//        else {
//            LOGGER.info("Registered theme '{}' from '{}'", name, source.getFilePath());
//        }
        return theme;
    }

    @Override
    public Theme getCurrentTheme() {
        return syncRegistryAccessor.getCurrentTheme();
    }

    @Override
    public String getCurrentThemeName() {
        return currentThemeName;
    }

    private @Nullable IRawTheme tryLoadTheme(IThemeSource source) {
        try {
            return RawThemeReader.readTheme(source);
        } catch (Exception e) {
            LOGGER.error("Failed to read theme from '{}'", source.getFilePath(), e);
        }
        return null;
    }

    @Override
    public Registry getTMRegistry() {
        return registry;
    }

    @Override
    public SyncRegistry getSyncRegistry() {
        return syncRegistry;
    }

    @Override
    public SyncRegistryAccessor getSyncRegistryAccessor() {
        return syncRegistryAccessor;
    }

    @Override
    public @Nullable Theme getTheme(String name) {
        return themes.get(name);
    }

    @Override
    public Set<String> getThemeList() {
        return themes.keySet();
    }

    @Override
    public boolean setTheme(String name) {
        LOGGER.info("Setting theme to '{}'", name);
        Theme theme = themes.get(name);
        if (theme == null) {
            LOGGER.warn("Was asked to set theme '{}' but it doesn't exist!", name);
            return false;
        }
        currentThemeName = name;
        getSyncRegistry().setTheme(theme);
        return true;
    }

    public static void init() {
        // Dummy method to call the static initializer
    }
}