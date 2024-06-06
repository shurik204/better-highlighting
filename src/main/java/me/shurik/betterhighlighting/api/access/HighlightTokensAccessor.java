package me.shurik.betterhighlighting.api.access;

import org.eclipse.tm4e.core.grammar.IToken;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;

/**
 * Tokenize the command for highlighting.
 */
public interface HighlightTokensAccessor {
    ITokenizeLineResult<IToken[]> highlight$getTokenizationResult();
}