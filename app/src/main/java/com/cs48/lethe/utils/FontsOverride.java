package com.cs48.lethe.utils;

import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;

/**
 * Changes the font on all parts of the application
 */
public final class FontsOverride {

    /**
     * Sets the default font for the whole application
     *
     * @param context Interface to global information about an application environment
     * @param staticTypefaceFieldName The name of the font in the system to override
     * @param fontAssetName The name of the custom font
     */
    public static void setDefaultFont(Context context,
                                      String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(),
                fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);
    }

    /**
     * Replaces the font in the application
     *
     * @param staticTypefaceFieldName The name of the font in the system to override
     * @param newTypeface The name of the custom font
     */
    protected static void replaceFont(String staticTypefaceFieldName,
                                      final Typeface newTypeface) {
        try {
            final Field staticField = Typeface.class
                    .getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}