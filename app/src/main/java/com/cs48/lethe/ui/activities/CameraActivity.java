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
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.server.PostPicture;
import com.cs48.lethe.ui.dialogs.NetworkUnavailableDialog;
import com.cs48.lethe.ui.dialogs.OperationFailedDialog;
import com.cs48.lethe.utils.FileUtilities;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;

import java.io.File;
import java.io.IOException;

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
    public static final int SUCCESSFUL_POST = 300;

    private File mImageFile;
    private MenuItem mPostButton;
    private boolean mCurrentlyPosting;
    private DatabaseHelper mDatabaseHelper;

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

        mDatabaseHelper = DatabaseHelper.getInstance(this);
        mCurrentlyPosting = false;
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
        imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImageFile)); // set the image file name

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
        if (!mCurrentlyPosting) {
            goBack();
            super.onBackPressed();
        }
    }

    /**
     * Navigation back button in action bar
     */
    @Override
    public boolean onSupportNavigateUp() {
        if (!mCurrentlyPosting) {
            goBack();
            return true;
        }
        return false;
    }

    /**
     * Actions performed after a called activity is finished.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CAPTURE_REQUEST) {
            // If user presses okay on camera, then it saves it to storage
            if (resultCode == RESULT_OK) {
                Picasso.with(this).load(mImageFile).into(mImageView);
                // if user cancels image capture, then return to main screen
            } else if (resultCode == RESULT_CANCELED) {
                if (mImageFile != null)
                    mImageFile.delete();
                setResult(RESULT_CANCELED);
                finish();
            }
        }
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
//                postPicture();
                new PostPicture(this, mImageFile).execute();
                return true;
            } else {
                new NetworkUnavailableDialog().show(getFragmentManager(), TAG);
                return false;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * NOT FULLY WORKING
     * Post the picture on the server and stores the
     * image in the internal database.
     */
    public void postPicture() {
        mProgressBar.setVisibility(View.VISIBLE);
        mCurrentlyPosting = true;
        try {
            String url = getString(R.string.server) + getString(R.string.server_post);
            String[] coordinates = FileUtilities.getLocationCoordinates(this);

            RequestParams params = new RequestParams();
            params.put("avatar", mImageFile);
            params.put("latitude", coordinates[0]);
            params.put("longitude", coordinates[1]);

            AsyncHttpClient client = new AsyncHttpClient();
            client.post(url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    mProgressBar.setVisibility(View.GONE);
                    mCurrentlyPosting = false;






                    setResult(SUCCESSFUL_POST);
                    finish();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    mProgressBar.setVisibility(View.GONE);
                    mCurrentlyPosting = false;
                    new OperationFailedDialog().show(getFragmentManager(), TAG);
                    FileUtilities.logResults(CameraActivity.this, TAG, "code = " + statusCode);
                    Log.d(TAG, error.getLocalizedMessage());
                    onPostPictureFailed();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hides the back button in the action bar. Hides the
     * post button. And changes the title to reflect
     * that the image is currently being posted
     * to the server.
     */
    public void onPostPicture() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mPostButton.setVisible(false);
        setTitle("Posting...");
    }

    /**
     * Shows the back button in the action bar. Shows the
     * post button. And changes the title back to the
     * normal title.
     */
    public void onPostPictureFailed() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPostButton.setVisible(true);
        setTitle(getString(R.string.title_activity_camera));
    }

}