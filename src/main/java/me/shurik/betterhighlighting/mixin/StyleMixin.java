package me.shurik.betterhighlighting.mixin;

import me.shurik.betterhighlighting.util.access.StyleModifierAccess;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Style.class)
public class StyleMixin implements StyleModifierAccess {
    @Shadow @Final @Mutable @Nullable Boolean bold;
    @Shadow @Final @Mutable @Nullable Boolean italic;
    @Shadow @Final @Mutable @Nullable Boolean underlined;
    @Shadow @Final @Mutable @Nullable Boolean strikethrough;

    @Override
    public Style highlight$updateModifiers(boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        return (Style) (Object) this;
    }
}