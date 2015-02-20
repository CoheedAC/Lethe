package com.cs48.lethe.server;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.utils.FileUtilities;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Asynchronously posts the image on the server and returns the
 * unique picture id.
 */
public class PostPicture extends AsyncTask<String, String, Integer> {

    public static final String TAG = PostPicture.class.getSimpleName();

    private final String boundary = "---------------------Boundary";
    private Context mContext;
    private String mImagePath;

    public PostPicture(Context context) {
        mContext = context;
    }

    /**
     * Posts the image on the server while getting the current
     * location so the image is posted in the correct region.
     */
    @Override
    protected Integer doInBackground(String... path) {
        mImagePath = path[0];
        Log.d("TFirst", "ad");
        try {
            URL address = new URL(mContext.getString(R.string.server) + mContext.getString(R.string.server_post));
            HttpURLConnection connection = (HttpURLConnection) (address.openConnection());

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("Accept", "application/json");
            Log.d("TFirst", "ad");
            OutputStream requestBody = connection.getOutputStream();
            Log.d("Progress", "MADEITTODATA");


            String latitude = generateForSimpleText("latitude", getLatitude());
            String longitude = generateForSimpleText("longitude", getLongitude());
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
            Log.d("Progress", "END");

            Log.d("ConnectionType", connection.getHeaderField("Content-Type"));
            Log.d("Response Code:", String.valueOf(connection.getResponseCode()));

            try {
                InputStream ISIS = connection.getInputStream();
                Log.d("ad", "yolo");
                Log.d("add", String.valueOf(ISIS.available()));
                buffer = new byte[ISIS.available()];
                ISIS.read(buffer, 0, bufferSize);
                Log.d("ad", "yoloswag");
                Log.d("RESPONSE:", new String(buffer));
            } catch (Exception e) {
                InputStream ISIS = connection.getErrorStream();

            }


            //DataInputStream results = (DataInputStream) connection.getInputStream();
            connection.disconnect();
            //String str =  results.readLine();
            //Toast.makeText(this,str, Toast.LENGTH_LONG).show();

        } catch (NetworkOnMainThreadException e) {
            Log.d("Error", "NetworkMain");

        } catch (Exception e) {
            Log.d("Error", "GeneralException");
            Log.d("Error", e.getLocalizedMessage());
            // test.setText(e.getMessage());
            //cToast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        Toast.makeText(mContext, "Pic posted!", Toast.LENGTH_SHORT).show();
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

    /**
     * Returns the current location latitude.
     */
    private String getLatitude() {
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(lm.getBestProvider(new Criteria(), true));
        if (location == null) {
            return "0.0";
        }
        return String.valueOf(location.getLatitude());

    }

    /**
     * Returns the current location longitude.
     */
    private String getLongitude() {
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(lm.getBestProvider(new Criteria(), true));
        if (location == null) {
            return "0.0";
        }
        return String.valueOf(location.getLongitude());
    }
}
