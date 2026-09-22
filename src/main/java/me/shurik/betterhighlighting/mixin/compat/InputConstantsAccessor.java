package me.shurik.betterhighlighting.mixin.compat;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = InputConstants.class, priority = Integer.MAX_VALUE)
public interface InputConstantsAccessor {
    @Dynamic
    @Invoker(value = "Lcom/mojang/blaze3d/platform/InputConstants;isKeyDown(Lcom/mojang/blaze3d/platform/Window;I)Z", remap = false)
    static boolean compat$isKeyDown(Window window, int key) {
        return false;
    }
}