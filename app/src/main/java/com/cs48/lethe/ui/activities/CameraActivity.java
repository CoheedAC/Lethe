package com.cs48.lethe.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.utils.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CameraActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener {

    public static final String TAG = CameraActivity.class.getSimpleName();
    public static final int IMAGE_CAPTURE_REQUEST = 100;
    public static final int IMAGE_POST_REQUEST = 200;

    private final String boundary = "---------------------Boundary";
    private final int MIN_HOURS = 6;
    private final int MAX_HOURS = 24;
    private int imageTimer;

    private Uri mImageUri;

    @InjectView(R.id.imageView) ImageView mImageView;
    @InjectView(R.id.timerTextView) TextView mTimerTextView;
    @InjectView(R.id.seekBar) SeekBar mSeekBar;
    @InjectView(R.id.minHourTextView) TextView mMinHourTextView;
    @InjectView(R.id.maxHourTextView) TextView mMaxHourTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ButterKnife.inject(this);

        mMinHourTextView.setText(MIN_HOURS + "");
        mMaxHourTextView.setText(MAX_HOURS + "");

        // Offset seekbar by MIN_HOURS because seekbar
        // can only start at 0 (but allowed to set max value)
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(MAX_HOURS - MIN_HOURS);
        mSeekBar.setProgress(mSeekBar.getMax());

        // Displays back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Starts image capture
        startCamera();
    }

    /**
     * Starts built-in camera functionality and sets path to store file
     */
    private void startCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File imageFile = FileUtilities.saveImageFile(this); // create a file to save the image
        mImageUri = Uri.fromFile(imageFile); // gets Uri of saved image
        imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri); // set the image file name

        // start the image capture Intent
        startActivityForResult(imageCaptureIntent, IMAGE_CAPTURE_REQUEST);
    }

    /**
     * Hardware back button
     */
    @Override
    public void onBackPressed() {
        goBack();
        super.onBackPressed();
    }

    private void goBack() {
        deleteImage();
        setResult(RESULT_CANCELED);
        startCamera();
        mSeekBar.setProgress(mSeekBar.getMax());
    }

    /**
     * Navigation back button in action bar
     */
    @Override
    public boolean onSupportNavigateUp() {
        goBack();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_CAPTURE_REQUEST) {
            // If user presses okay on camera, then it saves it to storage
            if (resultCode == RESULT_OK) {
                mImageView.setImageURI(mImageUri);
                // if user cancels image capture, then return to main screen
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Changes timer text when user scrolls seekbar
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        mTimerTextView.setText("Set Timer: " + (progress + MIN_HOURS) + " hours");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /**
     * Stores the progress with MIN_HOURS as a lower boundary in a variable
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        imageTimer = (seekBar.getProgress() + MIN_HOURS);
    }

    /**
     * Delete image from path location
     */
    public boolean deleteImage() {
        File imageToDelete = new File(mImageUri.getPath());
        return imageToDelete.delete();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Returns to main screen and prints out image location if user presses post button
        if (id == R.id.action_post) {
            //new ImageClass().execute();//send request with imagedata to server
            Toast.makeText(this, mImageUri.toString(), Toast.LENGTH_LONG).show();
            Log.d(TAG, mImageUri.toString());
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Unimplemented
    private class ImageClass extends AsyncTask<String, String, Integer> {

        protected Integer doInBackground(String... params) {
            String imagePath = mImageUri.getPath();
            File imageFile = new File(mImageUri.getPath()); // stored as jpg
            Log.d("TFirst","ad");
            try {

                URL address = new URL("https://frozen-sea-8879.herokuapp.com/sendPic");
                HttpURLConnection connection = (HttpURLConnection) (address.openConnection());

                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("Accept", "text/html; charset=utf-8");
                Log.d("TFirst","ad");
                OutputStream requestBody = connection.getOutputStream();
                Log.d("Progress","MADEITTODATA");


                String latitude = generateForSimpleText("latitude", getLatitude());
                String longitude = generateForSimpleText("longitude", getLongitude());
                String combined = latitude + longitude;
                byte[] writer = combined.getBytes();
                requestBody.write(writer, 0, writer.length);



                String frontBoilerForImage = generateImageBoilerplateFront("Test.jpg");
                writer = frontBoilerForImage.getBytes();
                requestBody.write(writer, 0, writer.length);

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

                String endBoilerForImage = generateImageBoilerPlateEnd();
                writer = endBoilerForImage.getBytes();
                requestBody.write(writer, 0, writer.length); //finish image

                imageAsStream.close();
                requestBody.flush();
                requestBody.close();
                Log.d("Progress","END");

                Log.d("ConnectionType",connection.getHeaderField("Content-Type"));
                Log.d("Response Code:", String.valueOf(connection.getResponseCode()));

                try {
                    InputStream ISIS = connection.getInputStream();
                }
                catch(Exception e){
                    InputStream ISIS = connection.getErrorStream();
                    Log.d("ad","yolo");
                    buffer = new byte[ISIS.available()];
                    ISIS.read(buffer,0,bufferSize);
                    Log.d("ad","yoloswag");
                    Log.d("RESPONSE:",new String(buffer));
                }



                //DataInputStream results = (DataInputStream) connection.getInputStream();
                connection.disconnect();
                //String str =  results.readLine();
                //Toast.makeText(this,str, Toast.LENGTH_LONG).show();

            } catch (NetworkOnMainThreadException e) {
                Log.d("Error","NetworkMain");

            } catch (Exception e) {
                Log.d("Error","GeneralException");
                Log.d("Error",e.getLocalizedMessage());
                // test.setText(e.getMessage());
                //cToast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
            return 0;
        }
    }

    private String generateForSimpleText(String name, String value){
        return ("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" +value+"\r\n");
    }
    private String generateImageBoilerplateFront(String filename){
        return ("--" +boundary +"\r\nContent-Disposition: form-data; name=\"avatar\"; filename=\""+filename+"\"\r\nContent-Type: image/jpeg\r\n\r\n");
    }
    private String generateImageBoilerPlateEnd(){
        return ("\r\n--" + boundary + "--");
    }
    private String getLatitude(){
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(lm.getBestProvider(new Criteria(),true));
        if(location == null){
            return "0.0";
        }
        return String.valueOf(location.getLatitude());

    }
    private String getLongitude(){
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(lm.getBestProvider(new Criteria(),true));
        if(location == null){
            return "0.0";
        }
        return String.valueOf(location.getLongitude());
    }
}
