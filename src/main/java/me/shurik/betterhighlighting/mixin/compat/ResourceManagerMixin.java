package me.shurik.betterhighlighting.mixin.compat;

import me.shurik.betterhighlighting.util.access.ResourceManagerCompat;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings({"AddedMixinMembersNamePattern", "MissingUnique", "unused"})
@Mixin(value = ResourceManager.class, priority = -1000)
public interface ResourceManagerMixin extends ResourceManagerCompat {
    @Unique(silent = true)
    default Map<Identifier, Resource> listResources(String directory, ResourceManager.Selector selector) {
        throw new IllegalStateException("How did you get here?");
    }

    @Unique(silent = true)
    default Map<Identifier, Resource> listResources(String directory, Predicate<Identifier> selector) {
        // Directly calling results in "Ambiguous method call" so we jump through a dummy method
        return trampoline$listResources(directory, selector::test, null);
    }

    default Map<Identifier, Resource> trampoline$listResources(String directory, ResourceManager.Selector selector, Void $) {
        // This call lands fine due to previous Predicate -> ResourceManager.Selector conversion
        return listResources(directory, selector);
    }
}