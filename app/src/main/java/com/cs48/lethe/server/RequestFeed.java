package com.cs48.lethe.server;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.ui.adapters.FeedGridViewAdapter;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.Image;

import org.apache.http.HttpResponse;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Asynchronously downloads the JSON of all of the available
 * images in the current location.
 */
public class RequestFeed extends AsyncTask<String, Void, String> {

    public static final String TAG = RequestFeed.class.getSimpleName();

    private Context mContext;
    private FeedGridViewAdapter mFeedGridViewAdapter;

    public RequestFeed(Context context, FeedGridViewAdapter feedGridViewAdapter) {
        mContext = context;
        mFeedGridViewAdapter = feedGridViewAdapter;
    }

    /**
     * Requests to get the JSON in the background.
     */
    @Override
    protected String doInBackground(String... location) {
        String latitude = location[0].replace(".", "a");
        String longitude = location[1].replace(".", "a");
        String address = mContext.getResources().getString(R.string.server) + mContext.getString(R.string.server_recent) + longitude + "," + latitude;
        return getRequest(address);
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
        result = result.trim();
        if (!result.isEmpty()) {
            FileUtilities.logResults(mContext, TAG, "Request for image feed succeeded");
            try {
                List<Image> imageList = getPictures(result);

                DatabaseHelper databaseHelper = new DatabaseHelper(mContext);
                databaseHelper.createFeedTable(imageList);
                mFeedGridViewAdapter.fetchImageList();
            } catch (JSONException e) {
                Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            }
        } else {
            FileUtilities.logResults(mContext, TAG, "Request for image feed failed");
        }
    }

    /**
     * Parses the JSON string array and turns each entry into its own
     * Thumbnail object that holds the unique picture ID, the
     * DropBox URL, and the File of the image.
     */
    private List<Image> getPictures(String jsonData) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonData);
        List<Image> imageList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++)
            imageList.add(parseJSON(jsonArray.getJSONObject(i)));
        return imageList;
    }

    /**
     * Parses the JSON returned from the server and
     * returns a Thumbnail object
     */
    private Image parseJSON(JSONObject jsonObject) {
        try {
            String id = jsonObject.getString(mContext.getString(R.string.json_id));
            String thumbnailUrl = jsonObject.getString(mContext.getString(R.string.json_url_thumbnail));
            String fullUrl = jsonObject.getString(mContext.getString(R.string.json_url_full));
            int views = jsonObject.getInt(mContext.getString(R.string.json_views));
            int likes = jsonObject.getInt(mContext.getString(R.string.json_likes));
//            String datePosted = jsonObject.getString(mContext.getString(R.string.json_date_posted));

            return new Image(id, thumbnailUrl, fullUrl, views, likes);
//            return new Image(id, thumbnailUrl, fullUrl, views, likes, datePosted);
        } catch (JSONException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
        }
        return null;
    }
}