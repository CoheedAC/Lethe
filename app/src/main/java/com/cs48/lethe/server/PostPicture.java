package com.cs48.lethe.server;

import android.content.Context;
import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.ui.activities.PostPictureActivity;
import com.cs48.lethe.ui.dialogs.OperationFailedDialog;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.Image;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
public class PostPicture extends AsyncTask<String, String, String> {

    public static final String LOG_TAG = PostPicture.class.getSimpleName();

    private final String BOUNDARY = "---------------------Boundary";

    private PostPictureActivity mPostPictureActivity;
    private File mImageFile;

    public PostPicture(Context context, File imageFile) {
        mPostPictureActivity = (PostPictureActivity) context;
        mImageFile = imageFile;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mPostPictureActivity.onPostPictureStart();
    }

    /**
     * Posts the image on the server while getting the current
     * location so the image is posted in the correct region.
     */
    @Override
    protected String doInBackground(String... path) {
        HttpURLConnection connection = null;
        OutputStream requestBody = null;
        FileInputStream imageAsStream = null;
        try {
            URL url = new URL(mPostPictureActivity.getString(R.string.server) + mPostPictureActivity.getString(R.string.server_post));
            connection = (HttpURLConnection) (url.openConnection());

            // Allow inputs and outputs.
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Set HTTP method to POST.
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            connection.setRequestProperty("Accept", "application/json");

            requestBody = connection.getOutputStream();

            String[] coordinates = FileUtilities.getLocationCoordinates(mPostPictureActivity);
            String latitude = generateForSimpleText("latitude", coordinates[0]);
            String longitude = generateForSimpleText("longitude", coordinates[1]);
            String combined = latitude + longitude;

            byte[] writer = combined.getBytes();
            requestBody.write(writer, 0, writer.length);

            String frontBoilerForImage = generateImageBoilerplateFront(mImageFile.getName());
            writer = frontBoilerForImage.getBytes();
            requestBody.write(writer, 0, writer.length);

            //now encode image
            imageAsStream = new FileInputStream(mImageFile.getAbsolutePath());
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

            // return response from server
            return convertInputStreamToString(connection.getInputStream());

        } catch (NetworkOnMainThreadException e) {
            Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            return null;
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            return null;
        } finally {
            if (connection != null)
                connection.disconnect();
            try {
                if (imageAsStream != null)
                    imageAsStream.close();
                if (requestBody != null) {
                    requestBody.flush();
                    requestBody.close();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            }
        }
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
        super.onPostExecute(result);
        mPostPictureActivity.onPostPictureEnd();
        if (result != null && !result.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(mPostPictureActivity);
                databaseHelper.insertPostedImage(
                        new Image(jsonObject.getString(mPostPictureActivity.getString(R.string.json_id)),
                                jsonObject.getString(mPostPictureActivity.getString(R.string.json_date_posted)),
                                mImageFile, 0, 0));

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            }

            mPostPictureActivity.setResult(PostPictureActivity.POST_SUCCESS);
            mPostPictureActivity.finish();
        } else
            new OperationFailedDialog().show(mPostPictureActivity.getFragmentManager(), LOG_TAG);
    }

    private String generateForSimpleText(String name, String value) {
        return ("--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value + "\r\n");
    }

    private String generateImageBoilerplateFront(String filename) {
        return ("--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"avatar\"; filename=\"" + filename + "\"\r\nContent-Type: image/jpeg\r\n\r\n");
    }

    private String generateImageBoilerPlateEnd() {
        return ("\r\n--" + BOUNDARY + "--");
    }

}
