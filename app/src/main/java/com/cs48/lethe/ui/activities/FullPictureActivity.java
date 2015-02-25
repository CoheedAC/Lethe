package com.cs48.lethe.ui.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import com.cs48.lethe.ui.dialogs.AlreadyLikedImageDialog;
import com.cs48.lethe.ui.dialogs.OperationFailedDialog;
import com.cs48.lethe.ui.view_helpers.OnHorizontalSwipeTouchListener;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.Image;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * The activity that handles showing the full-sized image
 * whenever the user tabs on an image in the grid.
 */
public class FullPictureActivity extends ActionBarActivity {

    public static final String LOG_TAG = FullPictureActivity.class.getSimpleName();
    public static final String CACHED_IMAGE_INTERFACE = "CACHED_IMAGE_INTERFACE";
    public static final String POSTED_IMAGE_INTERFACE = "POSTED_IMAGE_INTERFACE";
    public static final int HIDE_PICTURE = -100;
    public static final int DELETE_PICTURE = -101;
    public static final int FULL_PICTURE_REQUEST = 100;

    private DatabaseHelper mDatabaseHelper;
    private Image mImage;

    @InjectView(R.id.fullImageView)
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
        setContentView(R.layout.activity_full_picture);

        ButterKnife.inject(this);

        // Get access to the database
        mDatabaseHelper = DatabaseHelper.getInstance(this);

        // Hide action bar and progress bar
        getSupportActionBar().hide();
        mProgressBar.setVisibility(View.GONE);

        // Get photo id from intent
        String uniqueId = getIntent().getStringExtra("uniqueId");

        String interfaceType = getIntent().getAction();
        if (interfaceType.equals(POSTED_IMAGE_INTERFACE))
            showPostedImage(uniqueId);
        else if (interfaceType.equals(CACHED_IMAGE_INTERFACE))
            showCachedImage(uniqueId);

        // Displays the statistics on the screen and fetches the statistics from the server
        mLikesTextView.setText("Likes: " + mImage.getLikes());
        mViewsTextView.setText("Views: " + mImage.getViews());
        if (FileUtilities.isNetworkAvailable(this))
            fetchPictureStatisticsFromServer();
    }

    /**
     * Shows the interface for an image pulled from the server
     */
    private void showCachedImage(String uniqueId) {
        // Hides the image action buttons
        mButtonLinearLayout.setVisibility(View.GONE);

        // Show the loading progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        // Database interactions
        mImage = mDatabaseHelper.getCachedImage(uniqueId);    // get image from feed table
        mDatabaseHelper.viewImage(mImage);    // update views in table

        // Display thumbnail image while full image loads
//        Picasso.with(this)
//                .load(mImage.getThumbnailUrl())
//                .resize(150, 0)
//                .onlyScaleDown()
//                .into(mImageView);

        // Display full image
        final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mProgressBar.setVisibility(View.GONE);

//                    if (bitmap.getHeight() < bitmap.getWidth())
//                        bitmap = FileUtilities.rotateBitmap(bitmap, 90);

                mImageView.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                mProgressBar.setVisibility(View.GONE);
                new OperationFailedDialog().show(getFragmentManager(), LOG_TAG);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        mImageView.setTag(target);
        Picasso.with(this)
                .load(mImage.getFullUrl())
                .resize(1024,0)
                .onlyScaleDown()
                .into(target);  // load image from url into imageview

        setUpOnSwipeListener();
    }

    /**
     * Shows the interface for an image posted to the server
     */
    private void showPostedImage(String uniqueId) {
        // Shows the image action buttons
        mButtonLinearLayout.setVisibility(View.VISIBLE);

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
                Toast.makeText(FullPictureActivity.this, "Deleted image", Toast.LENGTH_SHORT).show();
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
                    if (FileUtilities.saveImageForSharing(FullPictureActivity.this, mImage.getFile().getAbsolutePath()))
                        Toast.makeText(FullPictureActivity.this, "Saved picture to shared storage.", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(FullPictureActivity.this, "Picture already exists in shared storage", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(FullPictureActivity.this, "Cannot copy to shared storage.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Handles the swipe / tap gestures.
     */
    private void setUpOnSwipeListener() {
        mImageView.setOnTouchListener(new OnHorizontalSwipeTouchListener(this) {

            /**
             * Swiping left likes the photo then goes back to the feed.
             */
            @Override
            public void onSwipeLeft() {
                if (!mDatabaseHelper.isImageLiked(mImage)) {
                    mDatabaseHelper.likeImage(mImage);
                    likePicture();
                    finish();
                } else
                    new AlreadyLikedImageDialog().show(getFragmentManager(), LOG_TAG);
            }

            /**
             * Swiping right hides the photo from the feed and dislikes
             * it on the server, then returns to the feed.
             */
            @Override
            public void onSwipeRight() {
                mDatabaseHelper.hideImage(mImage);
                setResult(HIDE_PICTURE);
                dislikePicture();
                finish();
            }

            /**
             * Tapping anywhere on the screen goes back to the feed.
             */
            @Override
            public void onSingleTap() {
                finish();
            }
        });
    }

    /**
     * Likes the picture on the server
     */
    public void likePicture() {
        String url = getString(R.string.server_like) + mImage.getUniqueId();
        HerokuClient.get(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    FileUtilities.logResults(FullPictureActivity.this, LOG_TAG, "Liked pic!");
                    String jsonData = new String(responseBody);
                    JSONObject jsonObject = new JSONObject(jsonData);
                    mImage.setLikes(jsonObject.getInt(getString(R.string.json_likes)));
                    mImage.setViews(jsonObject.getInt(getString(R.string.json_views)));
                    mDatabaseHelper.updateStatisticsFromImage(mImage);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                FileUtilities.logResults(FullPictureActivity.this, LOG_TAG, "Failed to like pic!");
            }
        });
    }

    /**
     * Dislikes the picture on the server
     */
    public void dislikePicture() {
        String url = getString(R.string.server_dislike) + mImage.getUniqueId();
        HerokuClient.get(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    FileUtilities.logResults(FullPictureActivity.this, LOG_TAG, "Disliked pic!");
                    String jsonData = new String(responseBody);
                    JSONObject jsonObject = new JSONObject(jsonData);
                    mImage.setLikes(jsonObject.getInt(getString(R.string.json_likes)));
                    mImage.setViews(jsonObject.getInt(getString(R.string.json_views)));
                    mDatabaseHelper.updateStatisticsFromImage(mImage);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                FileUtilities.logResults(FullPictureActivity.this, LOG_TAG, "Failed to dislike pic!");
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
                FileUtilities.logResults(FullPictureActivity.this, LOG_TAG, "Request for statistics failed");
            }
        });
    }
}