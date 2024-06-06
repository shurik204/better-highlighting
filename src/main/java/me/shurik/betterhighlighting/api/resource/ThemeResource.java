package me.shurik.betterhighlighting.api.resource;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.eclipse.tm4e.core.registry.IThemeSource;

import java.io.IOException;
import java.io.Reader;

/**
 * TextMate theme source for Minecraft resource packs
 * @param location resource location
 * @param resource minecraft resource
 */
@MethodsReturnNonnullByDefault
public record ThemeResource(ResourceLocation location, Resource resource) implements IThemeSource {
    @Override
    public String getFilePath() {
        return location.toString();
    }

    @Override
    public Reader getReader() throws IOException {
        return resource.openAsReader();
    }
}