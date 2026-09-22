package me.shurik.betterhighlighting.util;

import net.minecraft.resources.Identifier;

public class CompatUtils {
    public static Identifier identifier(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}