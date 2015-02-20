package com.cs48.lethe;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cs48.lethe.utils.StorageType;

/**
 * Controls where the files are stored.
 */
public class ApplicationSettings {

    SharedPreferences mSharedPreferences;

    public ApplicationSettings(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Returns the current storage type.
     */
    public String getStoragePreference() {
        return mSharedPreferences.getString("Storage", StorageType.PRIVATE_EXTERNAL);
    }

    /**
     * Changes the storage type.
     */
    public void setSharedPreference (String storageType) {
        mSharedPreferences.edit().putString("Storage",storageType).apply();
    }

}
