package com.cs48.lethe.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.cs48.lethe.R;
import com.cs48.lethe.server.PostPicture;
import com.cs48.lethe.ui.dialogs.NetworkUnavailableDialog;
import com.cs48.lethe.utils.FileUtilities;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * An activity that handles all actions taken with the camera including
 * image capture, image file storing, and posting image.
 */
public class CameraActivity extends ActionBarActivity {

    public static final String TAG = CameraActivity.class.getSimpleName();
    public static final int IMAGE_CAPTURE_REQUEST = 100;
    public static final int IMAGE_POST_REQUEST = 200;

    private Uri mImageUri;
    private File mImageFile;
    private MenuItem mPostButton;

    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.imageView)
    ImageView mImageView;

    /**
     * Enables the action bar back button and starts the built-in
     * camera activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ButterKnife.inject(this);

        mProgressBar.setVisibility(View.GONE);

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

        mImageFile = FileUtilities.savePostedImage(this); // create a file to save the image
        mImageUri = Uri.fromFile(mImageFile); // gets Uri of saved image
        imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri); // set the image file name


        // start the image capture Intent
        startActivityForResult(imageCaptureIntent, IMAGE_CAPTURE_REQUEST);
    }

    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * Deletes the image that the user captured and restarts the
     * camera intent.
     */
    private void goBack() {
        if (mImageFile != null)
            mImageFile.delete();
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

    /**
     * Actions performed after a called activity is finished.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == IMAGE_CAPTURE_REQUEST) {
            // If user presses okay on camera, then it saves it to storage
            if (resultCode == RESULT_OK) {
                try {
                    mImageView.setImageBitmap(FileUtilities.getValidSizedBitmap(getContentResolver(),mImageUri));
                }
                catch(Exception e){
                    Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }

                // if user cancels image capture, then return to main screen
            } else if (resultCode == RESULT_CANCELED) {
                if (mImageFile != null)
                    mImageFile.delete();
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        mPostButton = menu.findItem(R.id.action_post);
        return true;
    }

    /**
     * Performs tasks when user presses a button on the action bar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Returns to main screen and prints out image location if user presses post button
        if (id == R.id.action_post) {
            if (FileUtilities.isNetworkAvailable(this)) {
                onPostPicture();
                new PostPicture(this).execute(mImageUri.getPath());
                return true;
            }else {
                new NetworkUnavailableDialog().show(getFragmentManager(), TAG);
                return false;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void onPostPicture() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mPostButton.setVisible(false);
        setTitle("Posting...");
    }

    public void onPostPictureFailed() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPostButton.setVisible(true);
        setTitle(getString(R.string.title_activity_camera));
    }

}