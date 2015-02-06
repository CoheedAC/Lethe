package com.cs48.lethe.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;

import com.cs48.lethe.ui.adapters.FeedGridViewAdapter;

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
public class DownloadImage extends AsyncTask<String, String, Integer> {

    public static final String TAG = DownloadImage.class.getSimpleName();

    private Context mContext;
    private BaseAdapter mBaseAdapter;

    public DownloadImage (Context context, BaseAdapter baseAdapter) {
        mBaseAdapter = baseAdapter;
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        FileUtilities.deleteCachedImages();
    }

    protected Integer doInBackground(String... urls) {
        for (int i = 0; i < urls.length; i++) {
            InputStream input = null;
            OutputStream output = null;
            try {
                URL url = new URL(urls[i]);
                input = url.openStream();
                byte[] buffer = new byte[1500];
                String filename = FileUtilities.getFileName(urls[i]);
                Log.d(TAG, "file[" + i + "] = " + filename);
                output = new FileOutputStream(FileUtilities.getCachedDirectory() + File.separator + filename);
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
        }
        return 0;
    }

    protected void onPostExecute(Integer integer) {
        if (mBaseAdapter instanceof FeedGridViewAdapter) {
            ((FeedGridViewAdapter) mBaseAdapter).update();
        }
    }
}