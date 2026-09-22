package me.shurik.betterhighlighting.resource;

import me.shurik.betterhighlighting.BetterHighlighting;
import me.shurik.betterhighlighting.api.TextMateResourceLoader;
import me.shurik.betterhighlighting.api.TextMateRegistry;
import me.shurik.betterhighlighting.api.resource.GrammarResource;
import me.shurik.betterhighlighting.api.resource.ThemeResource;
import me.shurik.betterhighlighting.util.CompatUtils;
import me.shurik.betterhighlighting.util.access.ResourceManagerCompat;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.eclipse.jdt.annotation.NonNullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@NonNullByDefault
public class TextMateResourceLoaderImpl implements PreparableReloadListener, TextMateResourceLoader {
    public static final TextMateResourceLoaderImpl INSTANCE = new TextMateResourceLoaderImpl();

    private final Event<TextMateResourceLoader.Callback> reloadEvent = EventFactory.createArrayBacked(TextMateResourceLoader.Callback.class, (listeners) -> (registry) -> {
        for (TextMateResourceLoader.Callback listener : listeners) { listener.invoke(registry); }
    });
    private final String grammarPath;
    private final String themePath;
    private final TextMateRegistry registry;
    private final Identifier id;

    public TextMateResourceLoaderImpl() {
        this(BetterHighlighting.MOD_ID, "grammar", "theme", TextMateRegistry.instance());
    }

    public TextMateResourceLoaderImpl(String modId, String grammarPath, String themePath, TextMateRegistry registry) {
        this.id = CompatUtils.identifier(modId, grammarPath);
        this.grammarPath = grammarPath;
        this.themePath = themePath;
        this.registry = registry;
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(id, this);
    }

    public Identifier id() {
        return id;
    }

    @Override
    public String getName() {
        return id.toString();
    }

    // Below 1.21.2 compatibility
    public CompletableFuture<Void> method_25931(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return this.method_25931(preparationBarrier, resourceManager, backgroundExecutor, gameExecutor);
    }

    // 1.21.2-1.21.8 compatibility
    public CompletableFuture<Void> method_25931(PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor) {
        // How to handle different file types?
        // 0. Only support one format
        // 1. Priority: .tmlanguage > .tmlanguage.json > .tmlanguage.yaml > .tmlanguage.yml
        // 2. Have a manifest file that specifies which file to load for each grammar
        // 3. \/ (current) load all files and let the registry handle it

        // Run the reload on a separate thread,
        return CompletableFuture.runAsync(() -> this.reloadResources((ResourceManagerCompat) resourceManager), backgroundExecutor)
                                // signal the preparation barrier,
                                .thenCompose(preparationBarrier::wait)
                                // and finally invoke the reload event
                                 .thenRunAsync(() -> reloadEvent.invoker().invoke(registry), gameExecutor);
    }

    // 1.21.10
    @Override
    public CompletableFuture<Void> reload(SharedState state, Executor prepareExecutor, PreparationBarrier reloadSynchronizer, Executor applyExecutor) {
        return method_25931(reloadSynchronizer, state.resourceManager(), prepareExecutor, applyExecutor);
    }

    private void reloadResources(ResourceManagerCompat resourceManager) {
        registry.reset();
        resourceManager.listResources(grammarPath, TextMateResourceLoaderImpl::isGrammar).forEach((id, resource) -> {
            registry.registerGrammar(new GrammarResource(id, resource));
        });
        resourceManager.listResources(themePath, TextMateResourceLoaderImpl::isTheme).forEach((id, resource) -> {
             registry.registerTheme(new ThemeResource(id, resource));
        });
        BetterHighlighting.LOGGER.info("Loaded {} grammars and {} themes", registry.getGrammarList().size(), registry.getThemeList().size());
    }

    private static boolean isGrammar(Identifier id) {
        return id.getNamespace().equals(BetterHighlighting.MOD_ID) && (id.getPath().endsWith(".tmlanguage") || id.getPath().endsWith(".tmlanguage.json") || id.getPath().endsWith(".tmlanguage.yaml") || id.getPath().endsWith(".tmlanguage.yml"));
    }

    private static boolean isTheme(Identifier id) {
        return id.getNamespace().equals(BetterHighlighting.MOD_ID) && (id.getPath().endsWith(".tmtheme") || id.getPath().endsWith(".tmtheme.json") || id.getPath().endsWith(".tmtheme.yaml") || id.getPath().endsWith(".tmtheme.yml"));
    }

    @Override
    public void registerReloadListener(Callback callback) {
        reloadEvent.register(callback);
    }

    @Override
    public TextMateRegistry getRegistry() {
        return registry;
    }

    public static void init() {
        // Dummy method to call the static initializer
    }
}