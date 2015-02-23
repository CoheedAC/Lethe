package com.cs48.lethe.server;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.cs48.lethe.R;
import com.cs48.lethe.ui.activities.FullPictureActivity;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.Image;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Asynchronously downloads the full sized image from the server
 */
public class RequestFullPicture extends AsyncTask<String, String, String> {

    public static final String TAG = RequestFullPicture.class.getSimpleName();

    private Context mContext;
    private ProgressBar mProgressBar;
    private Image mImage;

    public RequestFullPicture(Context context, ProgressBar progressBar, Image image) {
        mContext = context;
        mProgressBar = progressBar;
        mImage = image;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        FileUtilities.logResults(mContext,TAG,"Loading picture...");
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Downloads the image in the background and outputs the data
     * by overwriting the original thumbnail file with the
     * full sized image.
     */
    protected String doInBackground(String... uniqueId) {
        return getJSONData(uniqueId[0]);
    }

    /**
     * Stores the JSON text into a string and returns the JSON string.
     */
    private String getJSONData(String uniqueId) {
        InputStream inputStream = null;
        String result = "";
        try {
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            // make GET request to the given URL
            String address = mContext.getString(R.string.server) + uniqueId;
            HttpResponse httpResponse = httpclient.execute(new HttpGet(address));
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
     * Sets the imageview to the full sized image when done downloading.
     */
    @Override
    protected void onPostExecute(String result) {
        if (!result.isEmpty()) {
            ((FullPictureActivity) mContext).setImageView(parseJSON(result));
        } else {
            mProgressBar.setVisibility(View.GONE);
            FileUtilities.logResults(mContext, TAG, "Request for full image failed");
        }
    }


    /**
     * Parses the JSON returned from the server and
     * returns a FullPicture object
     */
    private Image parseJSON(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            String fullUrl = jsonObject.getString(mContext.getString(R.string.json_url_full));
            String id = jsonObject.getString(mContext.getString(R.string.json_id));
            int views = jsonObject.getInt(mContext.getString(R.string.json_views));
            int likes = jsonObject.getInt(mContext.getString(R.string.json_likes));
//            mImage.setId(id);
            mImage.setFullUrl(fullUrl);
            mImage.setViews(views);
            mImage.setLikes(likes);
            return mImage;
        } catch (JSONException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
        }
        return null;
    }

}