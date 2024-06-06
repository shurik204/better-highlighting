package me.shurik.betterhighlighting.mixin;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.ParseResults;
import me.shurik.betterhighlighting.api.BuiltinGrammar;
import me.shurik.betterhighlighting.api.syntax.Tokenizer;
import me.shurik.betterhighlighting.api.access.HighlightTokensAccessor;
import org.eclipse.tm4e.core.grammar.IToken;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Unique
@Mixin(value = ParseResults.class, remap = false)
public abstract class ParseResultsMixin<S> implements HighlightTokensAccessor {
    @Shadow
    @Final
    private ImmutableStringReader reader;

    private ITokenizeLineResult<IToken[]> highlight$tokenizationResult = null;

    @Override
    public ITokenizeLineResult<IToken[]> highlight$getTokenizationResult() {
        // TODO: supplying the rule stack causes weird behavior with brackets
        if (highlight$tokenizationResult == null) {
            highlight$tokenizationResult = Tokenizer.tokenize(reader.getString(), BuiltinGrammar.mcfunction());
        }
        return highlight$tokenizationResult;
    }
}