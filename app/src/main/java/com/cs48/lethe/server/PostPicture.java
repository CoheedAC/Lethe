package com.cs48.lethe.server;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import com.cs48.lethe.R;
import com.cs48.lethe.ui.activities.CameraActivity;
import com.cs48.lethe.ui.dialogs.OperationFailedDialog;
import com.cs48.lethe.utils.FileUtilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Asynchronously posts the image on the server and returns the
 * unique picture id.
 */
public class PostPicture extends AsyncTask<String, String, Integer> {

    public static final String TAG = PostPicture.class.getSimpleName();
    public static final int SUCCESS = 0;
    public static final int FAILED = 1;

    private final String boundary = "---------------------Boundary";
    private CameraActivity mCameraActivity;
    private String mImagePath;
    private String mUniqueId;
    private String mDatePosted;

    public PostPicture(Context context) {
        mCameraActivity = (CameraActivity) context;
    }

    @Override
    protected void onCancelled(Integer integer) {
        mCameraActivity.hideProgressBar();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mCameraActivity.showProgressBar();
    }

    /**
     * Posts the image on the server while getting the current
     * location so the image is posted in the correct region.
     */
    @Override
    protected Integer doInBackground(String... path) {
        mImagePath = path[0];
        Log.d(TAG, "first");
        try {
            URL address = new URL(mCameraActivity.getString(R.string.server) + mCameraActivity.getString(R.string.server_post));
            HttpURLConnection connection = (HttpURLConnection) (address.openConnection());

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("Accept", "application/json");
            Log.d(TAG, "first");
            OutputStream requestBody = connection.getOutputStream();
            Log.d(TAG, "progress: made it to data");

            String[] coordinates = FileUtilities.getLocationCoordinates(mCameraActivity);
            String latitude = generateForSimpleText("latitude", coordinates[0]);
            String longitude = generateForSimpleText("longitude", coordinates[1]);
            String combined = latitude + longitude;

            byte[] writer = combined.getBytes();
            requestBody.write(writer, 0, writer.length);

            String imageName = FileUtilities.getSimpleName(mImagePath);
            String frontBoilerForImage = generateImageBoilerplateFront(imageName);
            writer = frontBoilerForImage.getBytes();
            requestBody.write(writer, 0, writer.length);

            //now encode image
            FileInputStream imageAsStream = new FileInputStream(mImagePath);
            int bytesAvailable = imageAsStream.available();
            int bufferSize = Math.min(bytesAvailable, 1 * 1024 * 1024);
            byte[] buffer = new byte[bufferSize];
            int bytesRead = imageAsStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                requestBody.write(buffer, 0, bufferSize);
                bytesAvailable = imageAsStream.available();
                bufferSize = Math.min(bytesAvailable, 1 * 1024 * 1024);
                bytesRead = imageAsStream.read(buffer, 0, bufferSize);
            } //use buffer, write until image data is exhausted
            //finished writing image

            String endBoilerForImage = generateImageBoilerPlateEnd();
            writer = endBoilerForImage.getBytes();
            requestBody.write(writer, 0, writer.length); //finish image

            imageAsStream.close();
            requestBody.flush();
            requestBody.close();
            Log.d(TAG, "progress: END");
            Log.d(TAG, "ConnectionType: " + connection.getHeaderField("Content-Type"));

            // response from server
            InputStream inputStreamResponse = connection.getInputStream();
            String response = convertInputStreamToString(inputStreamResponse).trim();
            if (response.isEmpty())
                throw new FileNotFoundException("Response from server is empty");
            JSONObject jsonObject = new JSONObject(response);
            mUniqueId = jsonObject.getString("id");
            mDatePosted = jsonObject.getString("created");

            connection.disconnect();
        } catch (NetworkOnMainThreadException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            return FAILED;
        } catch (IOException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            return FAILED;
        } catch (JSONException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            return FAILED;
        }
        return SUCCESS;
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
    protected void onPostExecute(Integer integer) {
        mCameraActivity.hideProgressBar();
        if (integer == SUCCESS) {
            FileUtilities.logResults(mCameraActivity, TAG, "Posted pic successfully!");

            Log.d(TAG, "Response:");
            Log.d(TAG, "id: " + mUniqueId);
            Log.d(TAG, "created: " + mDatePosted);

            /*
            FeedDbHelper feedDbHelper = new FeedDbHelper(mCameraActivity);
            SQLiteDatabase db = feedDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MeEntry.COLUMN_NAME_PHOTO_ID, mUniqueId);
            values.put(MeEntry.COLUMN_NAME_POST_DATE, mDatePosted);
            values.put(MeEntry.COLUMN_NAME_VIEWS, 0);
            values.put(MeEntry.COLUMN_NAME_LIKES, 0);
            long newRowId = db.insert(MeEntry.TABLE_NAME, "null", values);
            */

            mCameraActivity.setResult(Activity.RESULT_OK);
            mCameraActivity.finish();
        } else {
            mCameraActivity.onPostPictureFailed();
            new OperationFailedDialog().show(mCameraActivity.getFragmentManager(), TAG);
        }
        super.onPostExecute(integer);
    }

    private String generateForSimpleText(String name, String value) {
        return ("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value + "\r\n");
    }

    private String generateImageBoilerplateFront(String filename) {
        return ("--" + boundary + "\r\nContent-Disposition: form-data; name=\"avatar\"; filename=\"" + filename + "\"\r\nContent-Type: image/jpeg\r\n\r\n");
    }

    private String generateImageBoilerPlateEnd() {
        return ("\r\n--" + boundary + "--");
    }

}
