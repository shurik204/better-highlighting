package me.shurik.betterhighlighting.mixin;

import com.mojang.brigadier.ParseResults;
import me.shurik.betterhighlighting.Config;
import me.shurik.betterhighlighting.api.syntax.Styler;
import me.shurik.betterhighlighting.api.TextMateRegistry;
import me.shurik.betterhighlighting.api.access.HighlightTokensAccessor;
import me.shurik.betterhighlighting.util.DebugRender;
import net.minecraft.client.gui.GuiGraphics;
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
    EditBox input;

    @Shadow
    @Nullable
    private ParseResults<SharedSuggestionProvider> currentParse;

    @Inject(method = "formatText", at = @At("HEAD"), cancellable = true)
    private static void replacementTextFormatting(ParseResults<SharedSuggestionProvider> parseResults, String part, int maxLength, CallbackInfoReturnable<FormattedCharSequence> info) {
        if (parseResults == null) { return; }
        ITokenizeLineResult<IToken[]> tokenizationResult = ((HighlightTokensAccessor) parseResults).highlight$getTokenizationResult();
        info.setReturnValue(Styler.tokensToCharSequence(tokenizationResult, part, parseResults.getReader().getString(), TextMateRegistry.instance().getCurrentTheme(), maxLength));
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderScopes(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo info) {
        if (Config.INSTANCE.enableScopesDebug && DebugRender.renderScopes(guiGraphics, mouseX, mouseY, this.input, currentParse)) {
            info.cancel();
        }
    }
}