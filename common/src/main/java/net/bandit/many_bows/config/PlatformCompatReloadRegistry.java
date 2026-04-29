package net.bandit.many_bows.config;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PlatformCompatReloadRegistry {
    private static final List<Runnable> PRELOADERS = new CopyOnWriteArrayList<>();
    private static final List<Runnable> RELOADERS = new CopyOnWriteArrayList<>();

    private PlatformCompatReloadRegistry() {
    }

    public static void register(Runnable preloader, Runnable reloader) {
        PRELOADERS.add(preloader);
        RELOADERS.add(reloader);
    }

    public static void preloadAll() {
        for (Runnable preloader : PRELOADERS) {
            preloader.run();
        }
    }

    public static int reloadAll() {
        for (Runnable reloader : RELOADERS) {
            reloader.run();
        }
        return RELOADERS.size();
    }
}