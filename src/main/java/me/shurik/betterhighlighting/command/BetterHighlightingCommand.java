package me.shurik.betterhighlighting.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.shurik.betterhighlighting.BetterHighlighting;
import me.shurik.betterhighlighting.Config;
import me.shurik.betterhighlighting.api.BuiltinGrammar;
import me.shurik.betterhighlighting.api.TextMateRegistry;
import me.shurik.betterhighlighting.api.syntax.Tokenizer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BetterHighlightingCommand {
    public static boolean debug = FabricLoader.getInstance().isDevelopmentEnvironment();

    public static final Map<String, Consumer<CommandContext<FabricClientCommandSource>>> DEBUG_COMMANDS = Map.of(
            "debug", BetterHighlightingCommand::debugToggle,
            "scopes", BetterHighlightingCommand::debugScopes,
            "reload", BetterHighlightingCommand::debugReloadConfig,
            "config", BetterHighlightingCommand::debugConfig
    );

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        RequiredArgumentBuilder<FabricClientCommandSource, String> themeArg = ClientCommandManager.argument("theme", StringArgumentType.greedyString()).suggests((context, builder) -> {
            for (String theme : TextMateRegistry.instance().getThemeList()) {
                if (theme.startsWith(builder.getRemaining())) { builder.suggest(theme); }
            }
            return builder.buildFuture();
        }).executes(BetterHighlightingCommand::tryChangeTheme);

        LiteralArgumentBuilder<FabricClientCommandSource> themeNode = ClientCommandManager.literal("theme").then(themeArg);

        RequiredArgumentBuilder<FabricClientCommandSource, String> debugNode = ClientCommandManager.argument("argument", StringArgumentType.word()).executes(context -> {
            String argument = StringArgumentType.getString(context, "argument");

            if (argument.equals("debug")) {
                debugToggle(context);
                return 0;
            }

            if (BetterHighlightingCommand.debug && DEBUG_COMMANDS.containsKey(argument)) {
                DEBUG_COMMANDS.get(argument).accept(context);
            } else {
                sendPrefixedFeedback(context.getSource(), "text.betterhighlighting.debug.unknown_subcommand", argument);
            }
            return 0;
        }).suggests((context, builder) -> {
            if (BetterHighlightingCommand.debug) {
                for (String command : DEBUG_COMMANDS.keySet()) {
                    if (command.startsWith(builder.getRemaining())) { builder.suggest(command); }
                }
            }
            return builder.buildFuture();
        });

        LiteralArgumentBuilder<FabricClientCommandSource> rootNode = ClientCommandManager.literal("betterhighlighting").executes(BetterHighlightingCommand::displayInfo).then(themeNode).then(debugNode);
        dispatcher.register(rootNode);
    }

    private static int displayInfo(CommandContext<FabricClientCommandSource> context) {
        ModMetadata metadata = FabricLoader.getInstance().getModContainer(BetterHighlighting.MOD_ID).orElseThrow().getMetadata();
        sendPrefixedFeedback(context.getSource(), "text.betterhighlighting.info", metadata.getVersion(), metadata.getAuthors().stream().map(Person::getName).collect(Collectors.joining(", ")));
        sendPrefixedFeedback(context.getSource(), "text.betterhighlighting.current_theme", Config.INSTANCE.currentTheme);
        BetterHighlightingCommand.displayThemeList(context);
        return 0;
    }

    private static void displayThemeList(CommandContext<FabricClientCommandSource> ctx) {
        Set<String> themes = TextMateRegistry.instance().getThemeList();
        sendPrefixedFeedback(ctx.getSource(), "text.betterhighlighting.themes.count", themes.size());
        for (String theme : themes) {
            // TODO: crashes on older versions
//            MutableComponent item = Component.translatable("text.betterhighlighting.themes.item", theme);
//            item.withStyle(style ->
//                style.withHoverEvent(new HoverEvent.ShowText(Component.translatable("text.betterhighlighting.themes.hover")))
//                     .withClickEvent(new ClickEvent.RunCommand("/betterhighlighting theme " + theme))
//            );
            sendPrefixedFeedback(ctx.getSource(), "text.betterhighlighting.themes.item", theme);
        }
    }

    private static Component getPrefix() {
        Style bracketStyle = TextMateRegistry.instance().getBracketStyles().getStyle(0);
        Component formattedName = Tokenizer.tokenizeAndFormatComponent(BetterHighlighting.MOD_NAME, BuiltinGrammar.mcfunction(), TextMateRegistry.instance().getCurrentTheme());
        return Component.literal("[").setStyle(bracketStyle).append(formattedName).append(Component.literal("] ").setStyle(bracketStyle));
    }

    private static int tryChangeTheme(CommandContext<FabricClientCommandSource> context) {
        String theme = StringArgumentType.getString(context, "theme");
        if (TextMateRegistry.instance().getTheme(theme) != null) {
            TextMateRegistry.instance().setTheme(theme);
            Config.INSTANCE.currentTheme = theme;
            Config.save();
            sendPrefixedFeedback(context.getSource(), "text.betterhighlighting.changed_theme", theme);
        } else {
            sendPrefixedFeedback(context.getSource(), "text.betterhighlighting.theme_not_found", theme);
        }
        return 0;
    }

    private static void sendPrefixedFeedback(FabricClientCommandSource source, String message, Object... args) {
        source.sendFeedback(Component.translatable("%s%s", getPrefix(), Component.translatable(message, args)));
    }

    private static void sendPrefixedFeedback(FabricClientCommandSource source, String message) {
        source.sendFeedback(Component.translatable("%s%s", getPrefix(), Language.getInstance().getOrDefault(message)));
    }

    private static void sendPrefixedFeedback(FabricClientCommandSource source, Component message) {
        source.sendFeedback(Component.translatable("%s%s", getPrefix(), message));
    }

    private static String getStateText(boolean state) {
        return state ? Language.getInstance().getOrDefault("text.betterhighlighting.toggle.enabled") : Language.getInstance().getOrDefault("text.betterhighlighting.toggle.disabled");
    }

    private static void debugToggle(CommandContext<FabricClientCommandSource> ctx) {
        BetterHighlightingCommand.debug = !BetterHighlightingCommand.debug;
        sendPrefixedFeedback(ctx.getSource(), "text.betterhighlighting.debug.state", getStateText(BetterHighlightingCommand.debug));
    }

    private static void debugScopes(CommandContext<FabricClientCommandSource> ctx) {
        Config.INSTANCE.enableScopesDebug = !Config.INSTANCE.enableScopesDebug;
        Config.save();
        sendPrefixedFeedback(ctx.getSource(), "text.betterhighlighting.debug.scopes", getStateText(Config.INSTANCE.enableScopesDebug));
    }

    private static void debugReloadConfig(CommandContext<FabricClientCommandSource> ctx) {
        Config.load();
        sendPrefixedFeedback(ctx.getSource(), "text.betterhighlighting.debug.reloaded_config");
    }


    private static void debugConfig(CommandContext<FabricClientCommandSource> ctx) {
        sendPrefixedFeedback(ctx.getSource(), "Begin config");
        for (Field field : Config.INSTANCE.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            try {
                field.setAccessible(true);
                ctx.getSource().sendFeedback(Component.translatable("%s: %s", field.getName(), field.get(Config.INSTANCE)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        sendPrefixedFeedback(ctx.getSource(), "End config");
    }
}