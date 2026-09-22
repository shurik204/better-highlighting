package me.shurik.betterhighlighting.mixin.compat;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(InputConstants.class)
public class InputConstantsMixin {
    @Unique
    private static boolean isKeyDown(final Window window, final int key) {
        return InputConstants.isKeyDown(key);
    }
}