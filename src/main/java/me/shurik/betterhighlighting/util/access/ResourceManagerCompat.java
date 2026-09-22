package me.shurik.betterhighlighting.util.access;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.util.Map;
import java.util.function.Predicate;

public interface ResourceManagerCompat {
    Map<Identifier, Resource> listResources(String directory, Predicate<Identifier> selector);
}