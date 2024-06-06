package me.shurik.betterhighlighting.mixin.tm4e;

import org.eclipse.tm4e.core.internal.grammar.Grammar;
import org.eclipse.tm4e.core.internal.registry.SyncRegistry;
import org.eclipse.tm4e.core.internal.theme.Theme;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = SyncRegistry.class, remap = false)
public interface SyncRegistryAccessor {
    @Accessor("_theme")
    Theme getCurrentTheme();

    @Accessor("_grammars")
    Map<String, Grammar> getGrammars();
}
