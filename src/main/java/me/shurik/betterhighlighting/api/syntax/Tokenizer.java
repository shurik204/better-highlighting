package me.shurik.betterhighlighting.api.syntax;

import me.shurik.betterhighlighting.api.BuiltinGrammar;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.IToken;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.internal.grammar.ScopeStack;
import org.eclipse.tm4e.core.internal.theme.Theme;

import java.util.List;

/**
 * Utility class to tokenize strings.
 */
public final class Tokenizer {

    /**
     * Tokenize a command
     * @param command command
     * @return result
     */
    public static ITokenizeLineResult<IToken[]> command(String command) {
        return tokenize(command, BuiltinGrammar.mcfunction());
    }

    /**
     * Tokenize a JSON string
     * @param json JSON string
     * @return result
     */
    public static ITokenizeLineResult<IToken[]> json(String json) {
        return tokenize(json, BuiltinGrammar.json());
    }

    /**
     * Tokenize a string with the given grammar
     * @param string string
     * @param grammar grammar
     * @return result
     */
    public static ITokenizeLineResult<IToken[]> tokenize(String string, IGrammar grammar) {
        return grammar.tokenizeLine(string);
    }

    /**
     * Tokenize a string with the given grammar, format with the given theme and return as formatted char sequence
     * @param string string
     * @param grammar grammar
     * @param theme theme
     * @return formatted char sequence
     */
    public static FormattedCharSequence tokenizeAndFormatChars(String string, IGrammar grammar, Theme theme) {
        var tokenizationResult = tokenize(string, grammar);
        return Styler.tokensToCharSequence(tokenizationResult, string, theme);
    }

    /**
     * Tokenize a string with the given grammar, format with the given theme and return as a list of components
     * @param string string
     * @param grammar grammar
     * @param theme theme
     * @return list of components
     */
    public static List<Component> tokenizeAndFormatComponents(String string, IGrammar grammar, Theme theme) {
        ITokenizeLineResult<IToken[]> tokenizationResult = tokenize(string, grammar);
        return Styler.tokensToComponents(tokenizationResult, string, theme);
    }

    public static Component tokenizeAndFormatComponent(String string, IGrammar grammar, Theme theme) {
        List<Component> components = tokenizeAndFormatComponents(string, grammar, theme);
        MutableComponent result = Component.empty();
        for (Component component : components) {
            result.append(component);
        }
        return result;
    }

    /**
     * Get the scope stack from a token
     * @param token token
     * @return new scope stack
     */
    public static ScopeStack getScopeStack(IToken token) {
        // TODO: should I cache the result?
        return ScopeStack.from(token.getScopes().toArray(new String[0]));
    }
}
