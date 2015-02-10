package com.cs48.lethe.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.cs48.lethe.ui.adapters.FeedGridViewAdapter;

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
 * Created by maxkohne on 2/5/15.
 */
public class RequestNewImages extends AsyncTask<String, Void, String> {

    public static final String TAG = RequestNewImages.class.getSimpleName();

    private Context mContext;
    private FeedGridViewAdapter mFeedGridViewAdapter;

    public RequestNewImages(Context context, FeedGridViewAdapter feedGridViewAdapter) {
        mContext = context;
        mFeedGridViewAdapter = feedGridViewAdapter;
    }

    @Override
    protected String doInBackground(String... urls) {
        return getRequest(urls[0]);
    }

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

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(mContext, "Received Json!", Toast.LENGTH_SHORT).show();
        FileUtilities.deleteCachedImages();
        try {
            Thumbnail[] thumbnails = getThumbnails(result);
            for (Thumbnail thumbnail: thumbnails) {
                new DownloadNewImage(mContext, mFeedGridViewAdapter, thumbnail.getId()).execute(thumbnail.getUrl());
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
        }

    }

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