package com.cs48.lethe.utils;

/**
 * Created by maxkohne on 3/9/15.
 */
public final class FlikApplication extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/MyriadPro.ttf");
    }
}
