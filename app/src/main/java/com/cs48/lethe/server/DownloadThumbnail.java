package com.cs48.lethe.server;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.cs48.lethe.ui.adapters.FeedGridViewAdapter;
import com.cs48.lethe.utils.FileUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by maxkohne on 2/5/15.
 */
public class DownloadThumbnail extends AsyncTask<String, String, Integer> {

    public static final String TAG = DownloadThumbnail.class.getSimpleName();

    private Context mContext;
    private FeedGridViewAdapter mFeedGridViewAdapter;
    private String mImageName;

    public DownloadThumbnail(Context context, FeedGridViewAdapter feedGridViewAdapter, String id) {
        mFeedGridViewAdapter = feedGridViewAdapter;
        mContext = context;
        mImageName = "IMG _" + id + ".jpg";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected Integer doInBackground(String... urls) {
        InputStream input = null;
        OutputStream output = null;
        try {
            URL url = new URL(urls[0]);
            input = url.openStream();
            byte[] buffer = new byte[1500];

            output = new FileOutputStream(FileUtilities.getCachedDirectory() + File.separator + mImageName);

            int bytesRead;
            while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
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

    protected void onPostExecute(Integer integer) {

        mFeedGridViewAdapter.update();
    }
}