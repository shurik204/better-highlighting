package me.shurik.betterhighlighting.mixin;

import com.google.common.collect.Lists;
import me.shurik.betterhighlighting.util.access.TooltipRenderingCompat;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;

import java.util.List;
import java.util.stream.Collectors;

// Provides old tooltip rendering function on newer versions
@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin implements TooltipRenderingCompat {
    @Unique
    public void renderTooltip(Font font, List<Component> lines, int x, int y) {
        ((GuiGraphicsExtractor) (Object) this).tooltip(font, lines.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .collect(Collectors.toCollection(Lists::newArrayList)), x, y, DefaultTooltipPositioner.INSTANCE ,null);
    }
}