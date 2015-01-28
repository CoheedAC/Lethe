package com.cs48.lethe.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CameraActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final int IMAGE_CAPTURE_REQUEST = 100;

    private final int MIN_HOURS = 6;
    private final int MAX_HOURS = 24;
    private int imageTimer;

    private Uri mImageUri;
    private static String boundary = "--------------------Boundary";

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

    // Starts built-in camera functionality and sets path to store file
    private void startCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File imageFile = FileUtilities.saveImageFile(); // create a file to save the image
        mImageUri = FileUtilities.getImageUri(imageFile); // gets Uri of saved image
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri); // set the image file name

        // start the image capture Intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST);
        }
    }

    // Deletes captured image and restarts the camera when the back button is pressed
    @Override
    public boolean onSupportNavigateUp(){
        deleteImage();
        startCamera();
        mSeekBar.setProgress(mSeekBar.getMax());
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CAPTURE_REQUEST) {
            // If user presses okay on camera, then it saves it to storage
            if (resultCode == RESULT_OK) {
                mImageView.setImageURI(mImageUri);
             // if user cancels image capture, then return to main screen
            }else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // Changes timer text when user scrolls seekbar
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        mTimerTextView.setText("Set Timer: " + (progress + MIN_HOURS) + " hours");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    // Stores the progress with MIN_HOURS as a lower boundary in a variable
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        imageTimer = (seekBar.getProgress() + MIN_HOURS);
    }

    // Delete image from path location
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

        // returns to main screen and prints out image location if user presses post button
        if (id == R.id.action_post) {
            sendImageDataToServer(); //remove this if crashing
            Toast.makeText(this, mImageUri.toString(), Toast.LENGTH_LONG).show();
            Log.d(TAG, mImageUri.toString());
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Unimplemented
    private void sendImageDataToServer() {
        String imagePath = mImageUri.getPath();
        File imageFile = new File(mImageUri.getPath()); // stored as jpg
        try{
            URL address = new URL("https://frozen-sea-8879.herokuapp.com/sendPic");
            HttpURLConnection connection = (HttpURLConnection)(address.openConnection());

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection","Keep-Alive");
            connection.setRequestProperty("Content-Type","multipart/form-data;boundary="+boundary);

            DataOutputStream requestBody = (DataOutputStream)connection.getOutputStream();

            String latitude = generateForSimpleText("latitude","45.5");
            String longitude = generateForSimpleText("longitude","43.3");

            requestBody.writeBytes(latitude+longitude); // write lat and long to stream

            String frontBoilerForImage = generateImageBoilerplateFront("Test.jpg");
            requestBody.writeBytes(frontBoilerForImage);

            //now encode image
            FileInputStream imageAsStream = new FileInputStream(imagePath);
            int bytesAvailable = imageAsStream.available();
            int bufferSize = Math.min(bytesAvailable, 1*1024*1024);
            byte[] buffer = new byte[bufferSize];
            int bytesRead = imageAsStream.read(buffer,0,bufferSize);
            while(bytesRead>0){
                requestBody.write(buffer,0,bufferSize);
                bytesAvailable = imageAsStream.available();
                bufferSize = Math.min(bytesAvailable, 1*1024*1024);
                bytesRead = imageAsStream.read(buffer,0,bufferSize);
            } //use buffer, write until image data is exhausted
            //finished writing image

            String endBoilerForImage = generateImageBoilerPlateEnd();
            requestBody.writeBytes(endBoilerForImage); //finish image

            imageAsStream.close();
            requestBody.flush();
            requestBody.close();

            DataInputStream results = (DataInputStream)connection.getInputStream();
            String str =  results.readLine();
            Toast.makeText(this,str, Toast.LENGTH_LONG).show();

        }
        catch (Exception e){
            Toast.makeText(this, e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
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


}
