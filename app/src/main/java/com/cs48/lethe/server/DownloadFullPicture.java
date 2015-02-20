package com.cs48.lethe.server;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.FullPicture;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Asynchronously downloads the full sized image from the server.
 */
public class DownloadFullPicture extends AsyncTask<String, String, Integer> {

    public static final String TAG = DownloadThumbnail.class.getSimpleName();
    public static final int SUCCESS = 0;
    public static final int FAILED = 1;

    private Context mContext;
    private ImageView mImageView;
    private FullPicture mFullPicture;

    public DownloadFullPicture(Context context, ImageView imageView, FullPicture fullPicture) {
        mContext = context;
        mImageView = imageView;
        mFullPicture = fullPicture;
    }

    /**
     * Downloads the image in the background and outputs
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

            output = new FileOutputStream(mFullPicture.getFullPicture());

            int bytesRead;
            while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            return FAILED;
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
        return SUCCESS;
    }

    /**
     * Refreshes the feed grid whenever a new thumbnail is successfully
     * downloaded.
     */
    protected void onPostExecute(Integer integer) {
        if (integer == SUCCESS) {
            FileUtilities.logResults(mContext,TAG, "Download full image succeeded");
            Uri imageUri = Uri.fromFile(mFullPicture.getFullPicture());
            mImageView.setImageURI(imageUri);
        }else {
            FileUtilities.logResults(mContext,TAG, "Download full image failed");
        }
    }
}