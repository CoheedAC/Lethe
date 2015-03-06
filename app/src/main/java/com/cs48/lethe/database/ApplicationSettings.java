package com.cs48.lethe.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cs48.lethe.R;
import com.cs48.lethe.utils.StorageType;

/**
 * Settings for the application. Currently, this
 * only controls where the files are stored.
 */
public class ApplicationSettings {

    // Instance variables
    private SharedPreferences mSharedPreferences;
    private Context mContext;

    /**
     * Constructor that creates the settings for the application
     *
     * @param context Interface to global information about an application environment
     */
    public ApplicationSettings(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Sets the default storage to the private storage on the SD card
        setSharedPreference(StorageType.PRIVATE_EXTERNAL);
    }

    /**
     * Gets the type of storage to store files
     *
     * @return The type of storage. Defaults to PRIVATE_EXTERNAL
     *         if there is no storage preference already set.
     */
    public String getStoragePreference() {
        return mSharedPreferences.getString(mContext.getString(R.string.settings_storage), StorageType.PRIVATE_EXTERNAL);
    }

    /**
     * Changes the storage type.
     */
    public void setSharedPreference (String storageType) {
        mSharedPreferences.edit().putString(mContext.getString(R.string.settings_storage), storageType).apply();
    }

}
