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

import com.cs48.lethe.R;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.server.PostImage;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CameraActivity extends ActionBarActivity {

    public static final String TAG = CameraActivity.class.getSimpleName();
    public static final int IMAGE_CAPTURE_REQUEST = 100;
    public static final int IMAGE_POST_REQUEST = 200;

    private Uri mImageUri;

    @InjectView(R.id.imageView)
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ButterKnife.inject(this);

        // Displays back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Starts image capture
        startCamera();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Starts built-in camera functionality and sets path to store file
     */
    private void startCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File imageFile = FileUtilities.savePostedImage(this); // create a file to save the image
        mImageUri = Uri.fromFile(imageFile); // gets Uri of saved image
        imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri); // set the image file name

        // start the image capture Intent
        startActivityForResult(imageCaptureIntent, IMAGE_CAPTURE_REQUEST);
    }

    private void goBack() {
        FileUtilities.deleteImage(mImageUri);
        setResult(RESULT_CANCELED);
        startCamera();
    }

    /**
     * Hardware back button
     */
    @Override
    public void onBackPressed() {
        goBack();
        super.onBackPressed();
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
            // Tim, add code to PostImage class
            new PostImage(this).execute(mImageUri.getPath()); //send request with imagedata to server
            Log.d(TAG, mImageUri.toString());
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}