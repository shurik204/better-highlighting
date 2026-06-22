package me.shurik.betterhighlighting.mixin;

import com.mojang.brigadier.ParseResults;
import me.shurik.betterhighlighting.Config;
import me.shurik.betterhighlighting.api.syntax.Styler;
import me.shurik.betterhighlighting.api.TextMateRegistry;
import me.shurik.betterhighlighting.api.access.HighlightTokensAccessor;
import me.shurik.betterhighlighting.util.DebugRender;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.util.FormattedCharSequence;
import org.eclipse.tm4e.core.grammar.IToken;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Unique
@Mixin(CommandSuggestions.class)
public class CommandSuggestionsMixin {
    @Final
    @Shadow
    private EditBox input;

    @Shadow
    @Nullable
    private ParseResults<SharedSuggestionProvider> currentParse;

    @Inject(method = "formatText", at = @At("HEAD"), cancellable = true)
    private static void replacementTextFormatting(ParseResults<SharedSuggestionProvider> currentParse, String text, int offset, CallbackInfoReturnable<FormattedCharSequence> info) {
        if (currentParse == null) { return; }
        ITokenizeLineResult<IToken[]> tokenizationResult = ((HighlightTokensAccessor) currentParse).highlight$getTokenizationResult();
        info.setReturnValue(Styler.tokensToCharSequence(tokenizationResult, text, currentParse.getReader().getString(), TextMateRegistry.instance().getCurrentTheme(), offset));
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void renderScopes(GuiGraphicsExtractor gr, int mouseX, int mouseY, CallbackInfo info) {
        if (Config.INSTANCE.enableScopesDebug && DebugRender.renderScopes(gr, mouseX, mouseY, this.input, currentParse)) {
            info.cancel();
        }
    }
}