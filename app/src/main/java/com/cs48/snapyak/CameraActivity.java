package com.cs48.snapyak;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.cs48.snapyak.utils.FileUtilities;

import java.io.File;

public class CameraActivity extends ActionBarActivity {

    public static final String TAG = CameraActivity.class.getSimpleName();
    private static final int IMAGE_CAPTURE_REQUEST = 100;
    private ImageView mImageView;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mImageView = (ImageView) findViewById(R.id.imageView);

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

    // Delete image on back button or cancel of camera
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
            Toast.makeText(this, mImageUri.toString(), Toast.LENGTH_LONG).show();
            Log.d(TAG, mImageUri.toString());
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
