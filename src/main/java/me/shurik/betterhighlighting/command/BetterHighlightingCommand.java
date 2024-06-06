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
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

public class BetterHighlightingCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        RequiredArgumentBuilder<FabricClientCommandSource, String> themeArg = ClientCommandManager.argument("theme", StringArgumentType.greedyString()).suggests((context, builder) -> {
            for (String theme : TextMateRegistry.instance().getThemeList()) {
                if (theme.startsWith(builder.getRemaining())) { builder.suggest(theme); }
            }
            return builder.buildFuture();
        }).executes(BetterHighlightingCommand::tryChangeTheme);

        LiteralArgumentBuilder<FabricClientCommandSource> themeNode = ClientCommandManager.literal("theme").executes(context -> {
            sendPrefixedFeedback(context.getSource(), "", Config.INSTANCE.currentTheme);
            return 0;
        }).then(themeArg);

        RequiredArgumentBuilder<FabricClientCommandSource, String> debugNode = ClientCommandManager.argument("argument", StringArgumentType.word()).executes(context -> {
            String argument = StringArgumentType.getString(context, "argument");
            if (argument.equals("scope") || argument.equals("scopes")) {
                Config.INSTANCE.enableScopesDebug = !Config.INSTANCE.enableScopesDebug;
                Config.save();
                sendPrefixedFeedback(context.getSource(), "text.betterhighlighting.debug.scopes", Component.translatable(Config.INSTANCE.enableScopesDebug ? "addServer.resourcePack.enabled" : "addServer.resourcePack.disabled"));
            }
            if (argument.equals("reload")) {
                sendPrefixedFeedback(context.getSource(), "text.betterhighlighting.debug.reload");
                Config.load();
            }
            if (argument.equals("config")) {
                sendPrefixedFeedback(context.getSource(), "Begin config");
                for (Field field : Config.INSTANCE.getClass().getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) continue;
                    try {
                        field.setAccessible(true);
                        context.getSource().sendFeedback(Component.translatable("%s: %s", field.getName(), field.get(Config.INSTANCE)));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                sendPrefixedFeedback(context.getSource(), "End config");
            }
            if (argument.equals("themes")) {
                Set<String> themes = TextMateRegistry.instance().getThemeList();
                sendPrefixedFeedback(context.getSource(), "text.betterhighlighting.debug.themes", themes.size());
                for (String theme : themes) {
                    sendPrefixedFeedback(context.getSource(), "text.betterhighlighting.debug.theme", theme);
                }
            }
            return 0;
        });

        LiteralArgumentBuilder<FabricClientCommandSource> rootNode = ClientCommandManager.literal("betterhighlighting").then(themeNode).then(debugNode);
        dispatcher.register(rootNode);
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
}
