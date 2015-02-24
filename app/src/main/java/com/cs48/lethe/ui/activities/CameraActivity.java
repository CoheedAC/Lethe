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
import com.cs48.lethe.server.HerokuClient;
import com.cs48.lethe.ui.dialogs.NetworkUnavailableDialog;
import com.cs48.lethe.ui.dialogs.OperationFailedDialog;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.Image;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * An activity that handles all actions taken with the camera including
 * image capture, image file storing, and posting image.
 */
public class CameraActivity extends ActionBarActivity {

    public static final String LOG_TAG = CameraActivity.class.getSimpleName();
    public static final int IMAGE_CAPTURE_REQUEST = 100;
    public static final int IMAGE_POST_REQUEST = 200;
    public static final int POST_SUCCESS = 300;
    public static final int POST_FAILED = -300;

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
                onPostPictureStart();
                postPicture();
//                new PostPicture(this, mImageFile).execute();
                return true;
            } else {
                new NetworkUnavailableDialog().show(getFragmentManager(), LOG_TAG);
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
        onPostPictureStart();

        try {
            Log.d(LOG_TAG, "file: " + mImageFile.getAbsolutePath());
            Log.d(LOG_TAG, "name: " + mImageFile.getName());
            Log.d(LOG_TAG, "exists: " + mImageFile.exists());
            Log.d(LOG_TAG, "size: " + mImageFile.length() + " bytes");


            String[] coordinates = FileUtilities.getLocationCoordinates(this);
            RequestParams params = new RequestParams();
            params.put("avatar", mImageFile); // , "image/jpeg");
            params.put("latitude", coordinates[0]);
            params.put("longitude", coordinates[1]);


            HerokuClient.post(getString(R.string.server_post), params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    mProgressBar.setVisibility(View.GONE);
                    mCurrentlyPosting = false;
                    onPostPictureDone();
                    try {
                        String jsonData = new String(responseBody);
                        JSONObject jsonObject = new JSONObject(jsonData);

                        mDatabaseHelper.insertPostedImage(
                                new Image(jsonObject.getString("id"),
                                        jsonObject.getString("created"),
                                        mImageFile, 0, 0));

                        setResult(POST_SUCCESS);

                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                    }
                    setResult(POST_SUCCESS);
                    finish();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.d(LOG_TAG, "Error : " + error.getLocalizedMessage());
                    Log.d(LOG_TAG, "Status code : " + statusCode);
                    for (Header header : headers)
                        Log.d(LOG_TAG, header.getName() + " : " + header.getValue());
                    String response = new String(responseBody);
                    Log.d(LOG_TAG, "Response : " + response);


                    mProgressBar.setVisibility(View.GONE);
                    mCurrentlyPosting = false;
                    setResult(POST_FAILED);
                    new OperationFailedDialog().show(getFragmentManager(), LOG_TAG);
                    onPostPictureDone();
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
        }
    }

    /**
     * Hides the back button in the action bar. Hides the
     * post button. And changes the title to reflect
     * that the image is currently being posted
     * to the server.
     */
    public void onPostPictureStart() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mPostButton.setVisible(false);
        setTitle("Posting...");
    }

    /**
     * Shows the back button in the action bar. Shows the
     * post button. And changes the title back to the
     * normal title.
     */
    public void onPostPictureDone() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPostButton.setVisible(true);
        setTitle(getString(R.string.title_activity_camera));
    }

}