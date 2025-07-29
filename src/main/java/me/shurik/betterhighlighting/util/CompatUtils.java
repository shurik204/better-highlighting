package me.shurik.betterhighlighting.util;

import me.shurik.betterhighlighting.mixin.ResourceLocationAccessor;
import net.minecraft.resources.ResourceLocation;

public class CompatUtils {
    public static ResourceLocation location(String namespace, String path) {
        return ResourceLocationAccessor.create(ResourceLocationAccessor.assertValidNamespace(namespace, path), ResourceLocationAccessor.assertValidPath(namespace, path));
    }
}