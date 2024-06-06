package me.shurik.betterhighlighting.mixin.tm4e;

import org.eclipse.tm4e.core.internal.registry.SyncRegistry;
import org.eclipse.tm4e.core.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Registry.class, remap = false)
public interface RegistryAccessor {
    @Accessor("_syncRegistry")
    SyncRegistry getSyncRegistry();
}