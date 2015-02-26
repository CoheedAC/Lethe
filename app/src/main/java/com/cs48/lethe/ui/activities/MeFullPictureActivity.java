package com.cs48.lethe.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.networking.HerokuRestClient;
import com.cs48.lethe.utils.ActionCodes;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.PictureUtilities;
import com.cs48.lethe.utils.NetworkUtilities;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * The activity that handles showing the full-sized image
 * whenever the user tabs on an image in the grid of posted images.
 */
public class MeFullPictureActivity extends ActionBarActivity {

    public static final String LOG_TAG = MeFullPictureActivity.class.getSimpleName();

    private DatabaseHelper mDatabaseHelper;
    private List<Picture> mPictureList;
    private int mCurrentPosition;

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

    /**
     * Hides the action bar and gets all of the necessary data from
     * the Bundle that was passed from the calling activity. Shows/hides
     * buttons on the screen depending upon which view state was requested.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_full_picture);

        ButterKnife.inject(this);

        // Hide action bar and progress bar
        getSupportActionBar().hide();
        mProgressBar.setVisibility(View.GONE);

        mCurrentPosition = getIntent().getIntExtra("position", 0);

        // Set up list from the database
        mDatabaseHelper = DatabaseHelper.getInstance(this);
        mPictureList = mDatabaseHelper.getMePictures();

        // Display full image
        int rotationDegrees = PictureUtilities.getImageOrientation(mPictureList.get(mCurrentPosition).getFile().getAbsolutePath());
        Picasso.with(this)
                .load(mPictureList.get(mCurrentPosition).getFile()).
                rotate(rotationDegrees)
                .resize(1024, 0)
                .onlyScaleDown()
                .into(mImageView);

        setUpOnClickListeners();

        // Displays the statistics on the screen
        mLikesTextView.setText("Likes: " + mPictureList.get(mCurrentPosition).getLikes());
        mViewsTextView.setText("Views: " + mPictureList.get(mCurrentPosition).getViews());

        // fetches the statistics from the server
        if (NetworkUtilities.isNetworkAvailable(this))
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
                setResult(ActionCodes.DELETE_PICTURE, getIntent());
                mDatabaseHelper.deleteMePicture(mPictureList.get(mCurrentPosition));
                Toast.makeText(MeFullPictureActivity.this, "Deleted image", Toast.LENGTH_SHORT).show();
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
                    if (FileUtilities.saveImageForSharing(MeFullPictureActivity.this, mPictureList.get(mCurrentPosition).getFile().getAbsolutePath()))
                        Toast.makeText(MeFullPictureActivity.this, "Saved picture to shared storage.", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MeFullPictureActivity.this, "Picture already exists in shared storage", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(MeFullPictureActivity.this, "Cannot copy to shared storage.", Toast.LENGTH_SHORT).show();
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
        HerokuRestClient.get(mPictureList.get(mCurrentPosition).getUniqueId(), null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String jsonData = new String(responseBody);
                    JSONObject jsonObject = new JSONObject(jsonData);
                    mPictureList.get(mCurrentPosition).setViews(jsonObject.getInt("view"));
//                    mImageList.get(mCurrentPosition).setViews(jsonObject.getInt(getString(R.string.json_views)));
                    mPictureList.get(mCurrentPosition).setLikes(jsonObject.getInt(getString(R.string.json_likes)));

                    mDatabaseHelper.updateStatisticsFromImage(mPictureList.get(mCurrentPosition));

                    mLikesTextView.setText("Likes: " + mPictureList.get(mCurrentPosition).getLikes());
                    mViewsTextView.setText("Views: " + mPictureList.get(mCurrentPosition).getViews());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                FileUtilities.logResults(MeFullPictureActivity.this, LOG_TAG, "Request for statistics failed");
            }
        });
    }

}