package me.shurik.betterhighlighting.mixin;

import com.google.common.collect.Lists;
import me.shurik.betterhighlighting.util.access.TooltipRenderingCompat;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.*;

import java.util.List;
import java.util.stream.Collectors;

// Provides old tooltip rendering function on newer versions
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin implements TooltipRenderingCompat {
    @Unique
    public void renderTooltip(Font font, List<Component> lines, int x, int y) {
        this.method_51435(font, lines, x, y);
    }

    @Unique
    private void method_51435(Font font, List<Component> lines, int x, int y) {
        List<ClientTooltipComponent> tooltipLines = lines.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .collect(Collectors.toCollection(Lists::newArrayList));
        this.method_51435(font, tooltipLines, x, y, DefaultTooltipPositioner.INSTANCE);
    }

    /////////////////////////
    // Compat methods for older versions
    @Unique(silent = true)
    public void method_51435/*renderTooltip*/(Font font, List<ClientTooltipComponent> lines, int x, int y, ClientTooltipPositioner positioner) {
        ((GuiGraphics) (Object) this).renderTooltip(font, lines, x, y, positioner, null);
    }

//    @Unique(silent = true)
//    public void method_51435/*renderTooltip*/(Font font, List<ClientTooltipComponent> lines, int x, int y, ClientTooltipPositioner positioner, ResourceLocation background) {
//        throw new IllegalArgumentException("Compatibility method renderTooltipInternal called! Are you using a supported version of Minecraft?");
//    }
    ////////////////////////
}