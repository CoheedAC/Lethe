package com.cs48.lethe.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * A class dealing with networking utilities
 */
public class NetworkUtilities {

    // Logcat tag
    public static final String TAG = NetworkUtilities.class.getSimpleName();

    /**
     * Checks whether there is internet connectivity or not.
     *
     * @param context Interface to global information about an application environment
     * @return True if there is internet. False otherwise.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }




}
