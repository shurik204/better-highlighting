package me.shurik.betterhighlighting.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.ParseResults;
import me.shurik.betterhighlighting.api.syntax.Styler;
import me.shurik.betterhighlighting.api.TextMateRegistry;
import me.shurik.betterhighlighting.api.access.HighlightTokensAccessor;
import me.shurik.betterhighlighting.mixin.EditBoxAccessor;
import me.shurik.betterhighlighting.util.access.TooltipRenderingCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.eclipse.tm4e.core.grammar.IToken;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.internal.theme.FontStyle;
import org.eclipse.tm4e.core.internal.theme.StyleAttributes;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class DebugRender {
    public static boolean renderScopes(GuiGraphics guiGraphics, int mouseX, int mouseY, EditBox input, @Nullable ParseResults<SharedSuggestionProvider> parseResults) {
        if (parseResults != null && Screen.hasAltDown()) {
            ITokenizeLineResult<IToken[]> tokenizationResult = ((HighlightTokensAccessor) parseResults).highlight$getTokenizationResult();
            String padded = input.getValue() + " ";
            Font font = ((EditBoxAccessor) input).getFont();

            int Y = input.getY() - 2;
            for (IToken token : tokenizationResult.getTokens()) {
                int startX;
                try {
                    startX = font.width(padded.substring(0, token.getStartIndex())) + input.getX();
                } catch (Exception e) {
                    Minecraft.getInstance().gui.setOverlayMessage(Component.literal("Error X: " + e.getMessage()), false);
                    return false;
                }
                int width;
                try {
                    // TODO: trailing space causes exception
                    width = font.width(padded.substring(token.getStartIndex(), token.getEndIndex()));
                } catch (Exception e) {
                    Minecraft.getInstance().gui.setOverlayMessage(Component.literal("Error width: " + e.getMessage()), false);
                    return false;
                }
                int endX = startX + width;
                guiGraphics.fill(startX, Y, endX, Y + 1, token.getScopes().hashCode() * 11);
                if (input.getCursorPosition() <= token.getEndIndex() && input.getCursorPosition() > token.getStartIndex()) {
                    List<Component> tooltipLines = Lists.newArrayList(Component.literal(padded.substring(token.getStartIndex(), token.getEndIndex()).replace(' ', '·')));
                    tooltipLines.add(Component.literal("------------"));
                    tooltipLines.add(Component.literal("Scopes:").withStyle(ChatFormatting.AQUA));
                    for (String scope : token.getScopes().reversed()) {
                        tooltipLines.add(Component.literal(scope));
                    }
                    StyleAttributes style = Styler.getTextMateStyle(token);
                    Style mcStyle = Styler.toMinecraftStyle(style, TextMateRegistry.instance().getCurrentTheme());
                    tooltipLines.add(Component.literal("------------"));
                    tooltipLines.add(Component.literal("Style: ").withStyle(ChatFormatting.AQUA).append(FontStyle.fontStyleToString(style.fontStyle)));
                    tooltipLines.add(Component.literal("Colors: "));
                    if (mcStyle.getColor() != null) {
                        tooltipLines.add(Component.literal("Minecraft: " + mcStyle.getColor()));
                    }
                    if (style.foregroundId == 0) {
                        tooltipLines.add(Component.literal("Foreground: not set"));
                    } else {
                        tooltipLines.add(Component.literal("Foreground (" + style.foregroundId + "): " + Styler.getColorString(TextMateRegistry.instance().getCurrentTheme(), style.foregroundId)));
                    }
                    if (style.backgroundId == 0) {
                        tooltipLines.add(Component.literal("Background: not set"));
                    } else {
                        tooltipLines.add(Component.literal("Background (" + style.backgroundId + "): " + Styler.getColorString(TextMateRegistry.instance().getCurrentTheme(), style.backgroundId)));
                    }

                    int tooltipY = Y - (10 * (tooltipLines.size() - 1)) - 3;
                    if (tooltipY < 0) {
                        tooltipY = Y + 40;
                    }

                    ((TooltipRenderingCompat) guiGraphics).renderTooltip(font, tooltipLines, startX, tooltipY);
                }
            }
            return true;
        }
        return false;
    }
}
