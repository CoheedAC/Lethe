package com.cs48.lethe.server;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.cs48.lethe.ui.adapters.FeedGridViewAdapter;
import com.cs48.lethe.utils.Thumbnail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Asynchronously downloads the thumbnail sized image from the server.
 */
public class DownloadThumbnail extends AsyncTask<String, String, Integer> {

    public static final String TAG = DownloadThumbnail.class.getSimpleName();

    private Context mContext;
    private FeedGridViewAdapter mFeedGridViewAdapter;
//    private String mImageName;
    private Thumbnail mThumbnail;

    public DownloadThumbnail(Context context, FeedGridViewAdapter feedGridViewAdapter, Thumbnail thumbnail) {
        mFeedGridViewAdapter = feedGridViewAdapter;
        mContext = context;
//        mImageName = "IMG _" + id + ".jpg";
        mThumbnail = thumbnail;
    }

    /**
     * Downloads the image thumbnail in the background and outputs
     * the data into a file stored in the cache directory
     * with the name "IMG_xxx...xxx.jpg" where xxx...xxx is the
     * unique picture id requested from the server.
     */
    protected Integer doInBackground(String... urls) {
        InputStream input = null;
        OutputStream output = null;
        try {
            URL url = new URL(urls[0]);
            input = url.openStream();
            byte[] buffer = new byte[1500];

            output = new FileOutputStream(mThumbnail.getThumbnailFile());

            int bytesRead;
            while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            }
        }
        return 0;
    }

    /**
     * Refreshes the feed grid whenever a new thumbnail is successfully
     * downloaded.
     */
    protected void onPostExecute(Integer integer) {
        mFeedGridViewAdapter.update();
    }
}