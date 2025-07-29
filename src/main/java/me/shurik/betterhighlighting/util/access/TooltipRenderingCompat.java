package me.shurik.betterhighlighting.util.access;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.List;

// GuiGraphicsMixin accessor mixin
public interface TooltipRenderingCompat {
    void renderTooltip(Font font, List<Component> lines, int x, int y);
}