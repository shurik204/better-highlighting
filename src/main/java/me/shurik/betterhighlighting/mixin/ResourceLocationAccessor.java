package me.shurik.betterhighlighting.mixin;

import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Identifier.class)
public interface ResourceLocationAccessor {
    @Invoker("<init>")
    static Identifier create(String namespace, String path) { throw new AssertionError("???"); }
    @Invoker("assertValidNamespace")
    static String assertValidNamespace(String namespace, String path) { throw new AssertionError("???"); }
    @Invoker("assertValidPath")
    static String assertValidPath(String namespace, String path) { throw new AssertionError("???"); }
}