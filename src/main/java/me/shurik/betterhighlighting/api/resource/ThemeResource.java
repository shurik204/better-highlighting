package me.shurik.betterhighlighting.api.resource;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tm4e.core.registry.IThemeSource;

import java.io.IOException;
import java.io.Reader;

/**
 * TextMate theme source for Minecraft resource packs
 * @param location resource location
 * @param resource minecraft resource
 */
@NonNullByDefault
public record ThemeResource(Identifier location, Resource resource) implements IThemeSource {
    @Override
    public String getFilePath() {
        return location.toString();
    }

    @Override
    public Reader getReader() throws IOException {
        return resource.openAsReader();
    }
}