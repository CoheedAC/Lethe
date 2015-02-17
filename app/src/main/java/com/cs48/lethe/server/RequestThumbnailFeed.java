package com.cs48.lethe.server;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.cs48.lethe.ui.adapters.FeedGridViewAdapter;
import com.cs48.lethe.utils.Thumbnail;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Asynchronously downloads the JSON of all of the available
 * images in the current location.
 */
public class RequestThumbnailFeed extends AsyncTask<String, Void, String> {

    public static final String TAG = RequestThumbnailFeed.class.getSimpleName();

    private Context mContext;
    private FeedGridViewAdapter mFeedGridViewAdapter;

    public RequestThumbnailFeed(Context context, FeedGridViewAdapter feedGridViewAdapter) {
        mContext = context;
        mFeedGridViewAdapter = feedGridViewAdapter;
    }

    /**
     * Requests to get the JSON in the background.
     */
    @Override
    protected String doInBackground(String... urls) {
        return getRequest(urls[0]);
    }

    /**
     * Stores the JSON text into a string and returns the JSON string.
     */
    private String getRequest(String url) {
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
        } catch (ClientProtocolException e) {
            Log.d(TAG, e.getClass() + ": " + e.getLocalizedMessage());
        } catch (IOException e) {
            Log.d(TAG, e.getClass() + ": " + e.getLocalizedMessage());
        }
        return result;
    }

    /**
     * Interprets the data coming from the server as a String.
     */
    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    /**
     * Parses through each JSON object and downloads each image into
     * the cache folder.
     */
    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(mContext, "Received Json!", Toast.LENGTH_SHORT).show();
//        FileUtilities.deleteCachedImages();

        try {
            Thumbnail[] thumbnails = getThumbnails(result);
            for (Thumbnail thumbnail : thumbnails)
                new DownloadThumbnail(mContext, mFeedGridViewAdapter, thumbnail.getId()).execute(thumbnail.getUrl());
        } catch (JSONException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
        }
    }

    /**
     * Parses the JSON string array and turns each entry into its own
     * Thumbnail object that holds the unique picture ID, the
     * DropBox URL, and the File of the image.
     */
    private Thumbnail[] getThumbnails(String jsonData) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonData);
        Thumbnail[] thumbnails = new Thumbnail[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String id = jsonObject.getString("name");
            String url = jsonObject.getString("thumb");
            thumbnails[i] = new Thumbnail(id, url);
            Log.d(TAG, id + " : " + url);
        }
        return thumbnails;
    }
}