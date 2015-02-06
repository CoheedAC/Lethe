package com.cs48.lethe.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.utils.DownloadImage;
import com.cs48.lethe.utils.FileUtilities;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by maxkohne on 1/29/15.
 */
public class FeedGridViewAdapter extends BaseAdapter {

    public static final String TAG = FeedGridViewAdapter.class.getSimpleName();

    private List<File> mImageList;
    private Context mContext;

    public FeedGridViewAdapter(Context context) {
        mContext = context;
        mImageList = FileUtilities.getCachedImages();

        String getRequest = "https://frozen-sea-8879.herokuapp.com/hot/34a4133292,-119a8609718";
        new HttpAsyncTask().execute(getRequest);
    }

    public static String GET(String url) {
        Log.d(TAG, "GET request started");
        InputStream inputStream = null;
        String result = "";
        try {
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            // convert inputstream to string
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

            Log.d(TAG, "GET request ended");
            Log.d(TAG, result);
        } catch (Exception e) {
            Log.d(TAG, e.getClass() + ": " + e.getLocalizedMessage());
        }

        return result;
    }

    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        Log.d(TAG, "convert inputstream to string started");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        Log.d(TAG, "convert inputstream to string ended");
        return result;

    }

    public int getCount() {
        return mImageList.size();
    }

    public Object getItem(int position) {
        return mImageList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = (ImageView) convertView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            GridView.LayoutParams imageParams = new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    300);
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setBackgroundColor(mContext.getResources().getColor(R.color.image_load));
        }

        Uri imageUri = Uri.fromFile(mImageList.get(position));
        imageView.setImageURI(imageUri);

        return imageView;
    }

    public void update() {
        mImageList = FileUtilities.getCachedImages();
        notifyDataSetChanged();
    }

    public void clearCache() {
        FileUtilities.deleteCachedImages();
        update();
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(mContext, "Received Json!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Received Json!");
            Log.d(TAG, result);
            String urls[] = {
                   "https://dl.dropboxusercontent.com/1/view/or369g8ztpatnrz/Apps/letheyak4/app/public/system/avatars/76/thumb/IMG_20150206_052016.jpg?dl=1"
            };
            new DownloadImage(mContext, FeedGridViewAdapter.this).execute(urls);
        }
    }


}