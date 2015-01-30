package com.cs48.lethe;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cs48.lethe.utils.StorageType;

/**
 * Created by maxkohne on 1/29/15.
 */
public class ApplicationSettings {

    SharedPreferences mSharedPreferences;

    public ApplicationSettings(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getStoragePreference() {
        // StorageType.INTERAL is bugged right now. DO NOT USE IT.
        return mSharedPreferences.getString("Storage", StorageType.PRIVATE_EXTERNAL);
    }

    public void setSharedPreference (String storageType) {
        mSharedPreferences.edit().putString("Storage",storageType).apply();
    }

}
