package me.shurik.betterhighlighting.util;

import me.shurik.betterhighlighting.mixin.ResourceLocationAccessor;
import net.minecraft.resources.Identifier;

public class CompatUtils {
    public static Identifier identifier(String namespace, String path) {
        return ResourceLocationAccessor.create(ResourceLocationAccessor.assertValidNamespace(namespace, path), ResourceLocationAccessor.assertValidPath(namespace, path));
    }
}