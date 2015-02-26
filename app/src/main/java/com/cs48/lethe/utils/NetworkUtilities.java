package com.cs48.lethe.utils;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by maxkohne on 2/26/15.
 */
public class NetworkUtilities {

    public static final String LOG_TAG = NetworkUtilities.class.getSimpleName();

    /**
     * Returns whether there is internet connectivity or not.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Returns the current latitude[0] and longitude[1]
     */
    public static String[] getLocationCoordinates(Context context) {
        String[] coordinates = new String[2];
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(lm.getBestProvider(new Criteria(), true));

        if (location != null) {
            coordinates[0] = String.valueOf(location.getLatitude());
            coordinates[1] = String.valueOf(location.getLongitude());
        } else {
            // default to isla vista coordinates
            coordinates[0] = "34.4133"; // latitude
            coordinates[1] = "-119.861"; // longitude
        }
        return coordinates;
    }
}
