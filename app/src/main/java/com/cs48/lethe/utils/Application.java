package com.cs48.lethe.utils;

/**
 * Class to override default Application class in order
 * to change the font of the whole app.
 */
public final class Application extends android.app.Application {

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Overrides the MONOSPACE font with custom font
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/MyriadPro.ttf");
    }
}
