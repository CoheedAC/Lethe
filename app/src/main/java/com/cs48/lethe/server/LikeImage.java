package com.cs48.lethe.server;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Asynchronously increments the view count of the photo
 * on the server.
 */
public class LikeImage extends AsyncTask<String, String, Integer> {

    public static final String TAG = LikeImage.class.getSimpleName();

    private Context mContext;

    public LikeImage(Context context) {
        mContext = context;
    }

    /**
     * UNIMPLEMENTED
     * Increments the view count on the server
     */
    protected Integer doInBackground(String... urls) {
        String likeUrl = urls[0];

        return 0;
    }

    /**
     * Alerts the user that the picture has been liked.
     */
    protected void onPostExecute(Integer integer) {
        Toast.makeText(mContext, "Liked pic!", Toast.LENGTH_SHORT).show();
    }
}