package me.shurik.betterhighlighting;

import com.google.gson.Gson;
import me.shurik.betterhighlighting.api.syntax.BracketColorizer;
import me.shurik.betterhighlighting.api.TextMateRegistry;
import me.shurik.betterhighlighting.util.ColorUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static me.shurik.betterhighlighting.BetterHighlighting.LOGGER;

public class Config {
    public static final String CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("better-highlighting.json").toString();
    protected static final Config DEFAULT_CONFIG = new Config();
    public static Config INSTANCE;

    //////////////////
    public String currentTheme = "Material";

    public Integer unmatchedBracketColor = 0xC13328;
    public List<Integer> bracketColors = List.of(ColorUtils.colorFromString("#F9D849"), ColorUtils.colorFromString("#CC76D1"), ColorUtils.colorFromString("#4A9DF7"));

    public boolean bracketIndependentColoring = true;
    public boolean enableScopesDebug = false;
    //////////////////

    public static void load() {
        try (FileReader reader = new FileReader(Config.CONFIG_PATH)) {
            // Load config from file
            INSTANCE = new Gson().fromJson(reader, Config.class);
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                LOGGER.warn("Config file not found, using default");
            } else {
                LOGGER.error("Failed to load config file, using default", e);
            }
            INSTANCE = new Config();
        }

        if (INSTANCE.bracketColors.isEmpty()) {
            LOGGER.warn("Bracket colors are empty, resetting to default");
            INSTANCE.bracketColors = DEFAULT_CONFIG.bracketColors;
        }

        if (INSTANCE.currentTheme.isBlank()) {
            LOGGER.warn("Current theme not set, resetting to default");
            INSTANCE.currentTheme = DEFAULT_CONFIG.currentTheme;
        }

        TextMateRegistry.instance().setBracketStyles(BracketColorizer.BracketStyles.fromColors(INSTANCE.bracketColors, INSTANCE.unmatchedBracketColor));
    }

    public static void save() {
        // Save config to file
        try (FileWriter writer = new FileWriter(Config.CONFIG_PATH)) {
            writer.write(new Gson().toJson(INSTANCE));
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }
}