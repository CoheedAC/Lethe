package com.cs48.lethe.ui.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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

    public static final String LOG_TAG = FullPictureActivity.class.getSimpleName();
    public static final String CACHED_IMAGE_INTERFACE = "CACHED_IMAGE_INTERFACE";
    public static final String POSTED_IMAGE_INTERFACE = "POSTED_IMAGE_INTERFACE";
    public static final int HIDDEN = -100;
    public static final int DELETE_IMAGE = -101;
    public static final int FULL_PICTURE_REQUEST = 100;

    private DatabaseHelper mDatabaseHelper;
    private Image mImage;

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

        mDatabaseHelper = DatabaseHelper.getInstance(this);

        // Hide action bar and progress bar
        getSupportActionBar().hide();
        mProgressBar.setVisibility(View.GONE);

        // Get intent and extras
        setResult(RESULT_OK, getIntent());
        String uniqueId = getIntent().getStringExtra("uniqueId");

        if (getIntent().getAction().equals(POSTED_IMAGE_INTERFACE)) {

            mImage = mDatabaseHelper.getPostedImage(uniqueId);  // get image from me table

            FileUtilities.logResults(this, LOG_TAG, mImage.getFile().getAbsolutePath());

            mDatabaseHelper.viewImage(mImage);    // update views in table

            final Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mImageView.setImageBitmap(FileUtilities.getValidSizedBitmap(bitmap));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };
            mImageView.setTag(target);

            Picasso.with(this).load(mImage.getFile()).into(target); // load image from file into imageview
            showMeUI();   // show buttons related to me UI
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

        } else {

            mImage = mDatabaseHelper.getCachedImage(uniqueId);    // get image from feed table
            mDatabaseHelper.viewImage(mImage);    // update views in table
            mProgressBar.setVisibility(View.VISIBLE);

            final Target thumbnailTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mImageView.setImageBitmap(FileUtilities.getThumbnailSizedBitmap(bitmap));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };
            mImageView.setTag(thumbnailTarget);
            Picasso.with(FullPictureActivity.this)
                    .load(mImage.getThumbnailUrl())
                    .into(thumbnailTarget);

            final Target fullTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mProgressBar.setVisibility(View.GONE);
                    mImageView.setImageBitmap(FileUtilities.getValidSizedBitmap(bitmap));
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
            mImageView.setTag(fullTarget);
            Picasso.with(this).load(mImage.getFullUrl()).into(fullTarget);  // load image from url into imageview

            showFeedUI();   // show buttons related to feed UI
            setUpGestureListener();
        }
        showStatistics();
    }

    private void showStatistics() {
        mLikesTextView.setText("Likes: " + mImage.getLikes());
        mViewsTextView.setText("Views: " + mImage.getViews());
        if (FileUtilities.isNetworkAvailable(this))
            fetchPictureStatistics();
    }

    /**
     * Handles the swipe / tap gestures.
     */
    private void setUpGestureListener() {
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
                setResult(HIDDEN, getIntent());
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
                FileUtilities.logResults(FullPictureActivity.this, LOG_TAG, "Liked pic!");
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
                FileUtilities.logResults(FullPictureActivity.this, LOG_TAG, "Disliked pic!");
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
    public void fetchPictureStatistics() {
        HerokuClient.get(mImage.getUniqueId(), null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String jsonData = new String(responseBody);
                    JSONObject jsonObject = new JSONObject(jsonData);
                    mImage.setViews(jsonObject.getInt("view"));
                    mImage.setLikes(jsonObject.getInt(getString(R.string.json_likes)));

                    mDatabaseHelper.updateDatabaseStatisticsFromImage(mImage);

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

    /**
     * Hides the delete and copy button but shows the
     * like button. Also handles like button presses.
     */
    private void showFeedUI() {
        mDeleteButton.setVisibility(View.GONE);
        mCopyButton.setVisibility(View.GONE);
    }

    /**
     * Hides the like button but shows the delete and
     * copy buttons. Handles the visible button presses.
     */
    private void showMeUI() {
        mDeleteButton.setVisibility(View.VISIBLE);
        mCopyButton.setVisibility(View.VISIBLE);

        /**
         * Deletes the image stored on the device and goes back to the grid.
         */
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(DELETE_IMAGE, getIntent());
                mDatabaseHelper.deletePostedImage(mImage);
                Toast.makeText(FullPictureActivity.this, "Deleted image (UNIMPLEMENTED)", Toast.LENGTH_SHORT).show();
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

}


