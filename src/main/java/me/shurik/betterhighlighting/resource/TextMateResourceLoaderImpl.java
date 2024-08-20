package me.shurik.betterhighlighting.resource;

import me.shurik.betterhighlighting.BetterHighlighting;
import me.shurik.betterhighlighting.api.TextMateResourceLoader;
import me.shurik.betterhighlighting.api.TextMateRegistry;
import me.shurik.betterhighlighting.api.resource.GrammarResource;
import me.shurik.betterhighlighting.api.resource.ThemeResource;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@MethodsReturnNonnullByDefault
public class TextMateResourceLoaderImpl implements IdentifiableResourceReloadListener, TextMateResourceLoader {
    public static final TextMateResourceLoaderImpl INSTANCE = new TextMateResourceLoaderImpl();

    private final Event<TextMateResourceLoader.Callback> reloadEvent = EventFactory.createArrayBacked(TextMateResourceLoader.Callback.class, (listeners) -> (registry) -> {
        for (TextMateResourceLoader.Callback listener : listeners) { listener.invoke(registry); }
    });
    private final String modId;
    private final String grammarPath;
    private final String themePath;
    private final TextMateRegistry registry;

    public TextMateResourceLoaderImpl() {
        this(BetterHighlighting.MOD_ID, "grammar", "theme", TextMateRegistry.instance());
    }

    public TextMateResourceLoaderImpl(String modId, String grammarPath, String themePath, TextMateRegistry registry) {
        this.modId = modId;
        this.grammarPath = grammarPath;
        this.themePath = themePath;
        this.registry = registry;
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(this);
    }

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation(modId, grammarPath);
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        // How to handle different file types?
        // 0. Only support one format
        // 1. Priority: .tmlanguage > .tmlanguage.json > .tmlanguage.yaml > .tmlanguage.yml
        // 2. Have a manifest file that specifies which file to load for each grammar
        // 3. \/ (current) load all files and let the registry handle it

        // Run the reload on a separate thread,
        return CompletableFuture.runAsync(() -> this.reloadResources(resourceManager), backgroundExecutor)
                                // signal the preparation barrier,
                                .thenCompose(preparationBarrier::wait)
                                // and finally invoke the reload event
                                 .thenRunAsync(() -> reloadEvent.invoker().invoke(registry), gameExecutor);
    }

    private void reloadResources(ResourceManager resourceManager) {
        registry.reset();
        resourceManager.listResources(grammarPath, TextMateResourceLoaderImpl::isGrammar).forEach((id, resource) -> {
            registry.registerGrammar(new GrammarResource(id, resource));
        });
        resourceManager.listResources(themePath, TextMateResourceLoaderImpl::isTheme).forEach((id, resource) -> {
             registry.registerTheme(new ThemeResource(id, resource));
        });
        BetterHighlighting.LOGGER.info("Loaded {} grammars and {} themes", registry.getGrammarList().size(), registry.getThemeList().size());
    }

    private static boolean isGrammar(ResourceLocation id) {
        return id.getNamespace().equals(BetterHighlighting.MOD_ID) && (id.getPath().endsWith(".tmlanguage") || id.getPath().endsWith(".tmlanguage.json") || id.getPath().endsWith(".tmlanguage.yaml") || id.getPath().endsWith(".tmlanguage.yml"));
    }

    private static boolean isTheme(ResourceLocation id) {
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