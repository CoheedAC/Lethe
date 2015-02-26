package com.cs48.lethe.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.networking.HerokuRestClient;
import com.cs48.lethe.networking.PostPicture;
import com.cs48.lethe.ui.dialogs.NetworkUnavailableDialog;
import com.cs48.lethe.ui.dialogs.OperationFailedDialog;
import com.cs48.lethe.utils.ActionCodes;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.PictureUtilities;
import com.cs48.lethe.utils.NetworkUtilities;
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
public class PostPictureActivity extends ActionBarActivity {

    public static final String LOG_TAG = PostPictureActivity.class.getSimpleName();

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
        setContentView(R.layout.activity_post_picture);

        ButterKnife.inject(this);

        mDatabaseHelper = DatabaseHelper.getInstance(this);
        mCurrentlyPosting = false;
        mProgressBar.setVisibility(View.GONE);

        // Displays back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mImageFile = new File(getIntent().getData().getPath());

        int rotationDegrees = PictureUtilities.getImageOrientation(mImageFile.getAbsolutePath());
        Picasso.with(this)
                .load(mImageFile)
                .resize(1024,0)
                .rotate(rotationDegrees)
                .onlyScaleDown()
                .into(mImageView);
    }

    /**
     * Deletes the image that the user captured and restarts the
     * camera intent.
     */
    private void goBack() {
        setResult(ActionCodes.POST_CANCELLED);
        mImageFile.delete();
        finish();
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
     * Inflate the menu; this adds items to the action bar if it is present.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_picture, menu);
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
            if (NetworkUtilities.isNetworkAvailable(this)) {
                onPostPictureStart();
//                postPicture();
                new PostPicture(this, mImageFile).execute();
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
        onPostPictureStart();

        try {
            Log.d(LOG_TAG, "file: " + mImageFile.getAbsolutePath());
            Log.d(LOG_TAG, "name: " + mImageFile.getName());
            Log.d(LOG_TAG, "exists: " + mImageFile.exists());
            Log.d(LOG_TAG, "size: " + mImageFile.length() + " bytes");


            String[] coordinates = NetworkUtilities.getLocationCoordinates(this);
            RequestParams params = new RequestParams();
            params.put("avatar", mImageFile, "image/jpeg");
            params.put("latitude", coordinates[0]);
            params.put("longitude", coordinates[1]);


            HerokuRestClient.post(getString(R.string.server_post), params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    onPostPictureEnd();
                    setResult(ActionCodes.POST_SUCCESS);

                    try {
                        String jsonData = new String(responseBody);
                        JSONObject jsonObject = new JSONObject(jsonData);

                        mDatabaseHelper.insertMePicture(
                                new Picture(jsonObject.getString(getString(R.string.json_id)),
                                        jsonObject.getString(getString(R.string.json_date_posted)),
                                        mImageFile, 0, 0));

                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                    }

                    finish();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    onPostPictureEnd();
                    mCurrentlyPosting = false;
                    setResult(ActionCodes.POST_FAILED);

                    Log.d(LOG_TAG, "Error : " + error.getLocalizedMessage());
                    Log.d(LOG_TAG, "Status code : " + statusCode);
                    for (Header header : headers)
                        Log.d(LOG_TAG, header.getName() + " : " + header.getValue());
                    String response = new String(responseBody);
                    Log.d(LOG_TAG, "Response : " + response);

                    new OperationFailedDialog().show(getFragmentManager(), LOG_TAG);

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
        mProgressBar.setVisibility(View.VISIBLE);
        mCurrentlyPosting = true;
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mPostButton.setVisible(false);
        setTitle("Posting...");
    }

    /**
     * Shows the back button in the action bar. Shows the
     * post button. And changes the title back to the
     * normal title.
     */
    public void onPostPictureEnd() {
        mProgressBar.setVisibility(View.GONE);
        mCurrentlyPosting = false;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPostButton.setVisible(true);
        setTitle(getString(R.string.title_activity_post_picture));
    }

}