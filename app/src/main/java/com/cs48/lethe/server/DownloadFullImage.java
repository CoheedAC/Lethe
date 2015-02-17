package com.cs48.lethe.server;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Asynchronously downloads the full sized image from the server
 */
public class DownloadFullImage extends AsyncTask<String, String, Integer> {

    public static final String TAG = DownloadFullImage.class.getSimpleName();

    private Context mContext;
    private Uri mImageUri;
    private ImageView mImageView;

    public DownloadFullImage(Context context, Uri imageUri, ImageView imageView) {
        mContext = context;
        mImageUri = imageUri;
        mImageView = imageView;
    }

    /**
     * Downloads the image in the background and outputs the data
     * by overwriting the original thumbnail file with the
     * full sized image.
     */
    protected Integer doInBackground(String... urls) {
        InputStream input = null;
        OutputStream output = null;
        try {
            URL url = new URL(urls[0]);
            input = url.openStream();
            byte[] buffer = new byte[1500];

            output = new FileOutputStream(mImageUri.getPath());

            int bytesRead;
            while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            return 1;
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
     * Sets the imageview to the full sized image when done downloading.
     */
    protected void onPostExecute(Integer integer) {
        Toast.makeText(mContext, "Downloaded full image", Toast.LENGTH_SHORT).show();
        mImageView.setImageURI(mImageUri);
    }
}
