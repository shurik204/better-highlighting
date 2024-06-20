package me.shurik.betterhighlighting;

import me.shurik.betterhighlighting.api.syntax.BracketColorizer;
import me.shurik.betterhighlighting.api.BuiltinGrammar;
import me.shurik.betterhighlighting.api.TextMateResourceLoader;
import me.shurik.betterhighlighting.command.BetterHighlightingCommand;
import me.shurik.betterhighlighting.resource.TextMateResourceLoaderImpl;
import me.shurik.betterhighlighting.resource.TextMateRegistryImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterHighlighting implements ClientModInitializer {
    public static final String MOD_NAME = "Better Highlighting";
    public static final String MOD_ID = "better-highlighting";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        TextMateResourceLoaderImpl.init();
        TextMateRegistryImpl.init();
        Config.load();
        TextMateResourceLoader.instance().registerReloadListener((registry) -> {
            if (!registry.setTheme(Config.INSTANCE.currentTheme)) {
                registry.setTheme(Config.DEFAULT_CONFIG.currentTheme);
                Config.INSTANCE.currentTheme = Config.DEFAULT_CONFIG.currentTheme;
                Config.save();
            }
            registry.setBracketStyles(BracketColorizer.BracketStyles.fromColors(Config.INSTANCE.bracketColors, Config.INSTANCE.unmatchedBracketColor));
        });
        //noinspection InstantiationOfUtilityClass
        new BuiltinGrammar();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> BetterHighlightingCommand.register(dispatcher));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}