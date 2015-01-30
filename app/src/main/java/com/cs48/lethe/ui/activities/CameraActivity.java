package com.cs48.lethe.ui.activities;

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

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CameraActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final int IMAGE_CAPTURE_REQUEST = 100;
    public static final int CAMERA_ACTIVITY_REQUEST = 101;

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
     Starts built-in camera functionality and sets path to store file
     */
    private void startCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File imageFile = FileUtilities.saveImageFile(this); // create a file to save the image
        mImageUri = FileUtilities.getImageUri(imageFile); // gets Uri of saved image
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri); // set the image file name

        // start the image capture Intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST);
        }
    }

    /**
     Hardware back button
     */
    @Override
    public void onBackPressed() {
        deleteImage();
        startCamera();
        mSeekBar.setProgress(mSeekBar.getMax());
        Log.d(TAG, "Back button pressed");
        Toast.makeText(this, "Back button pressed", Toast.LENGTH_LONG).show();
        super.onBackPressed();
    }

    /**
     Navigation back button in action bar
     */
    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CAPTURE_REQUEST) {
            // If user presses okay on camera, then it saves it to storage
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Pressed OKAY", Toast.LENGTH_SHORT).show();
                mImageView.setImageURI(mImageUri);
                // if user cancels image capture, then return to main screen
            }else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                finish();
            }
            else {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     Changes timer text when user scrolls seekbar
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
     Stores the progress with MIN_HOURS as a lower boundary in a variable
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        imageTimer = (seekBar.getProgress() + MIN_HOURS);
    }

    /**
     Delete image from path location
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
            sendImageDataToServer();
            Toast.makeText(this, mImageUri.toString(), Toast.LENGTH_LONG).show();
            Log.d(TAG, mImageUri.toString());
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Unimplemented
    private void sendImageDataToServer() {
        File imageFile = new File(mImageUri.getPath()); // stored as jpg
        double longitude = 0;
        double latitude = 0;
        // send imageTimer
    }
}
