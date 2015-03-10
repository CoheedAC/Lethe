package com.cs48.lethe.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by maxkohne on 2/26/15.
 */
public class NetworkUtilities {

    public static final String TAG = NetworkUtilities.class.getSimpleName();

    /**
     * Returns whether there is internet connectivity or not.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }




}
