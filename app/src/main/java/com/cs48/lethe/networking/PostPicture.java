package com.cs48.lethe.networking;

import android.content.Context;
import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.ui.alertdialogs.OperationFailedAlertDialog;
import com.cs48.lethe.ui.camera.CameraActivity;
import com.cs48.lethe.utils.NetworkUtilities;
import com.cs48.lethe.utils.Picture;

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
 * Asynchronously posts the image on the server and adds
 * the picture to the internal database.
 */
public class PostPicture extends AsyncTask<String, String, String> {

    // Logcat tag
    public static final String TAG = PostPicture.class.getSimpleName();

    // Boundary used for posting to the server
    private final String BOUNDARY = "---------------------Boundary";

    // Instance variables
    private CameraActivity mCameraActivity;
    private File mPictureFile;

    /**
     * Constructor that takes in the context and the picture file.
     *
     * @param context Interface to global information about an application environment
     * @param pictureFile The file of the picture taken by the user
     */
    public PostPicture(Context context, File pictureFile) {
        mCameraActivity = (CameraActivity) context;
        mPictureFile = pictureFile;
    }

    /**
     * Runs on the UI thread before doInBackground(Params...).
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Shows and hides certain UI elements when during post process
        mCameraActivity.onPostPictureStart();
    }

    /**
     * Performs a computation on a background thread. The specified parameters are
     * the parameters passed to execute(Params...) by the caller of this task.
     * This method can call publishProgress(Progress...) to publish updates on
     * the UI thread.
     *
     * @param path The parameters of the task.
     *
     * @return A result, defined by the subclass of this task.
     */
    @Override
    protected String doInBackground(String... path) {
        HttpURLConnection connection = null;
        OutputStream requestBody = null;
        FileInputStream imageAsStream = null;
        try {
            // Opens the connection to the URL
            URL url = new URL(mCameraActivity.getString(R.string.server) + mCameraActivity.getString(R.string.server_post));
            connection = (HttpURLConnection) (url.openConnection());

            // Allow inputs and outputs.
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Set HTTP method to POST.
            connection.setRequestMethod("POST");

            // Sets the type of request information
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            connection.setRequestProperty("Accept", "application/json");

            // Output stream to the server
            requestBody = connection.getOutputStream();

            // Get latitude and longitude
            String[] coordinates = NetworkUtilities.getCurrentLocation(mCameraActivity);
            String latitude = generateForSimpleText("latitude", coordinates[0]);
            String longitude = generateForSimpleText("longitude", coordinates[1]);
            String combined = latitude + longitude;

            // TODO: Tim, upload this orientation int to the server
//            int orientation = PictureUtilities.getImageOrientation(mPictureFile.getAbsolutePath());

            // Write the latitude and longitude to the server
            byte[] writer = combined.getBytes();
            requestBody.write(writer, 0, writer.length);

            // Write the picture file name to the server
            String frontBoilerForImage = generateImageBoilerplateFront(mPictureFile.getName());
            writer = frontBoilerForImage.getBytes();
            requestBody.write(writer, 0, writer.length);

            // Encode the picture
            imageAsStream = new FileInputStream(mPictureFile.getAbsolutePath());
            int bytesAvailable = imageAsStream.available();
            int bufferSize = Math.min(bytesAvailable, 1 * 1024 * 1024);
            byte[] buffer = new byte[bufferSize];
            int bytesRead = imageAsStream.read(buffer, 0, bufferSize);

            // Use buffer, write until image data is exhausted
            while (bytesRead > 0) {
                requestBody.write(buffer, 0, bufferSize);
                bytesAvailable = imageAsStream.available();
                bufferSize = Math.min(bytesAvailable, 1 * 1024 * 1024);
                bytesRead = imageAsStream.read(buffer, 0, bufferSize);
            }
            // Finished writing picture

            // Write the picture to the server
            String endBoilerForImage = generateImageBoilerPlateEnd();
            writer = endBoilerForImage.getBytes();
            requestBody.write(writer, 0, writer.length); //finish image

            // return response from server
            return convertInputStreamToString(connection.getInputStream());

        } catch (NetworkOnMainThreadException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            return null;
        } catch (IOException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            return null;
        } finally {
            // Close all connections
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
                Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Converts the input stream (the response from the server) into
     * a String that can be parsed.
     *
     * @param inputStream The InputStream response from the server
     * @return The String ready to be parsed
     * @throws IOException
     */
    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";

        // Adds each line from the response to the result
        while ((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    /**
     * Runs on the UI thread after doInBackground(Params...). The specified
     * result is the value returned by doInBackground(Params...). This method
     * won't be invoked if the task was cancelled.
     *
     * This parses the String result into JSON objects to be stored
     * into a Picture object and then sent to the internal database.
     *
     * @param result The result of the operation computed by doInBackground(Params...).
     */
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        // If the result exists and is not empty, then parse the JSON
        if (result != null && !result.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(mCameraActivity);

                // Adds the Picture from the JSON data into the database
                databaseHelper.insertPicture(new Picture(
                        jsonObject.getString(mCameraActivity.getString(R.string.json_id)),
                        jsonObject.getString(mCameraActivity.getString(R.string.json_date_posted)),
                        mPictureFile, 0, 0));
            } catch (JSONException e) {
                Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            }

            // Finished posting and go back to the Main Activity
            mCameraActivity.finish();
        } else {
            // Else result is a failure response from the server
            // so show post failed UI elements
            mCameraActivity.onPostPictureFailed();

            // Alert the user that the post failed
            try {
                new OperationFailedAlertDialog().show(mCameraActivity.getFragmentManager(), TAG);
            }catch (IllegalStateException e) {
                Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Gets the Content-Dispostition header for the post request
     * with a key-value pair for GPS coordinates
     *
     * @param name Key of the pair
     * @param value Value of the pair
     * @return String result that the server can parse
     */
    private String generateForSimpleText(String name, String value) {
        return ("--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value + "\r\n");
    }

    /**
     * Gets the Content-Dispostition header for the post request
     * to send the picture to the server
     *
     * @param filename Name of the file to send to the server
     * @return String result that the server can parse
     */
    private String generateImageBoilerplateFront(String filename) {
        return ("--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"avatar\"; filename=\"" + filename + "\"\r\nContent-Type: image/jpeg\r\n\r\n");
    }

    /**
     * Gets the boundary header for the post request
     * when the picture is sent to the server
     *
     * @return String result that the server can parse
     */
    private String generateImageBoilerPlateEnd() {
        return ("\r\n--" + BOUNDARY + "--");
    }

}
