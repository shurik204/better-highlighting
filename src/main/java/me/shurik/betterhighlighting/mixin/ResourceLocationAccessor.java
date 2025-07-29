package me.shurik.betterhighlighting.mixin;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ResourceLocation.class)
public interface ResourceLocationAccessor {
    @Invoker("<init>")
    static ResourceLocation create(String namespace, String path) { throw new AssertionError("???"); }
    @Invoker("assertValidNamespace")
    static String assertValidNamespace(String namespace, String path) { throw new AssertionError("???"); }
    @Invoker("assertValidPath")
    static String assertValidPath(String namespace, String path) { throw new AssertionError("???"); }
}