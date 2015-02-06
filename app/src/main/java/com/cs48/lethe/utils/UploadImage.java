package com.cs48.lethe.utils;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by maxkohne on 2/5/15.
 */
public class UploadImage extends AsyncTask<String, String, Integer> {

    public static final String TAG = UploadImage.class.getSimpleName();

    private final String boundary = "---------------------Boundary";
    private Context mContext;

    public UploadImage(Context context) {
        mContext = context;
    }

    protected Integer doInBackground(String... imagePaths) {
        String imagePath = imagePaths[0];
        try {
            URL address = new URL("https://frozen-sea-8879.herokuapp.com/sendPic");
            HttpURLConnection connection = (HttpURLConnection) (address.openConnection());

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            OutputStream requestBody = connection.getOutputStream();
            Log.d(TAG, "Connection success");

            Location location = getLocation();
            String latitude = generateForSimpleText("latitude", getLatitude(location));
            String longitude = generateForSimpleText("longitude", getLongitude(location));
            String combined = latitude + longitude;
            Log.d(TAG, "Location success");

            byte[] writer = combined.getBytes();
            requestBody.write(writer, 0, writer.length);

            String filename = FileUtilities.getFileName(imagePath);
            if (filename == null)
                filename = "test.jpg";
            Log.d(TAG, "File name success: " + filename);

            String frontBoilerForImage = generateImageBoilerplateFront(filename);
            writer = frontBoilerForImage.getBytes();
            requestBody.write(writer, 0, writer.length);
            Log.d(TAG, "Writer success");

            //now encode image
            FileInputStream imageAsStream = new FileInputStream(imagePath);
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
            Log.d(TAG, "Inputstream success");

            String endBoilerForImage = generateImageBoilerPlateEnd();
            writer = endBoilerForImage.getBytes();
            requestBody.write(writer, 0, writer.length); //finish image

            imageAsStream.close();
            requestBody.flush();
            requestBody.close();
            Log.d(TAG, "Close success");

            connection.getInputStream(); // throws FileNotFoundException but still uploads
            connection.disconnect();
            Log.d(TAG, "Image uploaded successfully");
        } catch (NetworkOnMainThreadException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
        }catch (NullPointerException e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            for(StackTraceElement elem: e.getStackTrace()) {
                Log.e(TAG, elem.toString());
            }
        }
        return 0;
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

    private Location getLocation() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(),true));
    }

    // Coords are for Isla Vista
    private String getLatitude(Location location) {
        if (location == null) {
            String latitude = "34.4133";
            Log.d(TAG, "latitude: " + latitude);
            return latitude;
        }
        return String.valueOf(location.getLatitude());
    }

    private String getLongitude(Location location) {
        if (location == null) {
            String longitude = "-119.861";
            Log.d(TAG, "latitude: " + longitude);
            return longitude;
        }
        return String.valueOf(location.getLongitude());
    }
}
