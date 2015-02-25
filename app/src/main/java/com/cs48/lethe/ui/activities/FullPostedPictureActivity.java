package com.cs48.lethe.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.server.HerokuClient;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.Image;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * The activity that handles showing the full-sized image
 * whenever the user tabs on an image in the grid of posted images.
 */
public class FullPostedPictureActivity extends ActionBarActivity {

    public static final String LOG_TAG = FullPostedPictureActivity.class.getSimpleName();
    public static final int DELETE_PICTURE = -50;
    public static final int SHOW_POSTED_PICTURE_REQUEST = 50;

    private DatabaseHelper mDatabaseHelper;
    private Image mImage;

    @InjectView(R.id.imageView)
    ImageView mImageView;
    @InjectView(R.id.deleteButton)
    ImageButton mDeleteButton;
    @InjectView(R.id.saveButton)
    ImageButton mCopyButton;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.likesTextView)
    TextView mLikesTextView;
    @InjectView(R.id.viewsTextView)
    TextView mViewsTextView;
    @InjectView(R.id.buttonLinearLayout)
    LinearLayout mButtonLinearLayout;

    /**
     * Hides the action bar and gets all of the necessary data from
     * the Bundle that was passed from the calling activity. Shows/hides
     * buttons on the screen depending upon which view state was requested.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_cached_picture);

        ButterKnife.inject(this);

        // Get access to the database
        mDatabaseHelper = DatabaseHelper.getInstance(this);

        // Hide action bar and progress bar
        getSupportActionBar().hide();
        mProgressBar.setVisibility(View.GONE);

        // Get photo id from intent
        String uniqueId = getIntent().getStringExtra("uniqueId");

        // Shows the image action buttons
        mButtonLinearLayout.setVisibility(View.VISIBLE);

        // Set up database interaction
        mImage = mDatabaseHelper.getPostedImage(uniqueId);  // get image from me table
        mDatabaseHelper.viewImage(mImage);    // update views in table

        // Display full image
        int rotationDegrees = FileUtilities.getImageOrientation(mImage.getFile().getAbsolutePath());
        Picasso.with(this)
                .load(mImage.getFile()).
                rotate(rotationDegrees)
                .resize(1024, 0)
                .onlyScaleDown()
                .into(mImageView); // load image from file into imageview

        setUpOnClickListeners();

        // Displays the statistics on the screen and fetches the statistics from the server
        mLikesTextView.setText("Likes: " + mImage.getLikes());
        mViewsTextView.setText("Views: " + mImage.getViews());
        if (FileUtilities.isNetworkAvailable(this))
            fetchPictureStatisticsFromServer();
    }

    /**
     * Handles the tab gestures
     */
    private void setUpOnClickListeners() {
        /**
         * Exits the full screen view
         */
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /**
         * Deletes the image stored on the device and goes back to the grid.
         */
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(DELETE_PICTURE, getIntent());
                mDatabaseHelper.deletePostedImage(mImage);
                Toast.makeText(FullPostedPictureActivity.this, "Deleted image", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        /**
         * Copies the image from the private external (or internal) storage
         * into the public storage where the apps can access the photo.
         */
        mCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (FileUtilities.saveImageForSharing(FullPostedPictureActivity.this, mImage.getFile().getAbsolutePath()))
                        Toast.makeText(FullPostedPictureActivity.this, "Saved picture to shared storage.", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(FullPostedPictureActivity.this, "Picture already exists in shared storage", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(FullPostedPictureActivity.this, "Cannot copy to shared storage.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Gets the image statistics from the server and updates
     * the internal database with the new statistics. Also
     * displays the new statistics on the screen.
     */
    public void fetchPictureStatisticsFromServer() {
        HerokuClient.get(mImage.getUniqueId(), null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String jsonData = new String(responseBody);
                    JSONObject jsonObject = new JSONObject(jsonData);
                    mImage.setViews(jsonObject.getInt("view"));
//                    mImage.setViews(jsonObject.getInt(getString(R.string.json_views)));
                    mImage.setLikes(jsonObject.getInt(getString(R.string.json_likes)));

                    mDatabaseHelper.updateStatisticsFromImage(mImage);

                    mLikesTextView.setText("Likes: " + mImage.getLikes());
                    mViewsTextView.setText("Views: " + mImage.getViews());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                FileUtilities.logResults(FullPostedPictureActivity.this, LOG_TAG, "Request for statistics failed");
            }
        });
    }

}