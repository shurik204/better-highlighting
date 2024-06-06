package me.shurik.betterhighlighting.api;

import me.shurik.betterhighlighting.resource.TextMateResourceLoaderImpl;

/**
 * TextMate resources loader.
 *
 * <p>
 *     Themes and grammars are loaded from resource packs.
 *     It looks for files in the following locations:
 *     <ul>
 *         <li>{@code assets/better-highlighting/grammar/}</li>
 *         <li>{@code assets/better-highlighting/theme/}</li>
 *     </ul>
 *     Loader supports PList ({@code .tmlanguage} and {@code .tmtheme}), JSON, and YAML formats.
 * </p>
 */
public interface TextMateResourceLoader {
    static TextMateResourceLoader instance() {
        return TextMateResourceLoaderImpl.INSTANCE;
    }

    /**
     * Register a listener to be called when resources are reloaded
     * @param callback callback
     */
    void registerReloadListener(Callback callback);

    /**
     * Get grammar registry instance that this loader uses
     * @return registry
     */
    TextMateRegistry getRegistry();

    /**
     * Event callback
     */
    @FunctionalInterface
    interface Callback {
        /**
         * Invoke the callback
         * @param registry loader's registry
         */
        void invoke(TextMateRegistry registry);
    }
}