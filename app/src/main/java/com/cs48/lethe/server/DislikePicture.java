package com.cs48.lethe.server;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.cs48.lethe.R;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 * Asynchronously decrements the view count of the photo
 * on the server.
 */
public class DislikePicture extends AsyncTask<String, String, Integer> {

    public static final String TAG = LikePicture.class.getSimpleName();
    public static final int SUCCESS = 0;
    public static final int FAILED = 1;

    private Context mContext;

    public DislikePicture(Context context) {
        mContext = context;
    }

    /**
     * Decrements the view count on the server
     */
    protected Integer doInBackground(String... uniqueId) {
        try {
            String address = mContext.getString(R.string.server) + mContext.getString(R.string.server_dislike) + uniqueId[0];
            HttpClient httpclient = new DefaultHttpClient();
            httpclient.execute(new HttpGet(address));
        } catch (IOException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            return FAILED;
        }
        return SUCCESS;
    }

    /**
     * Alerts the user that the picture has been hidden (i.e. disliked).
     */
    protected void onPostExecute(Integer integer) {
        if (integer == SUCCESS)
            Toast.makeText(mContext, "Pic now hidden!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(mContext, "Failed to hide pic!", Toast.LENGTH_SHORT).show();
    }
}
