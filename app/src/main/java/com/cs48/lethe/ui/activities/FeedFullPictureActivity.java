package com.cs48.lethe.ui.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.networking.HerokuRestClient;
import com.cs48.lethe.ui.dialogs.AlreadyLikedImageDialog;
import com.cs48.lethe.ui.dialogs.OperationFailedDialog;
import com.cs48.lethe.ui.view_helpers.OnHorizontalSwipeListener;
import com.cs48.lethe.utils.ActionCodes;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.NetworkUtilities;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * The activity that handles showing the full-sized image
 * whenever the user tabs on an image in the grid of feed images.
 */
public class FeedFullPictureActivity extends ActionBarActivity {

    public static final String LOG_TAG = FeedFullPictureActivity.class.getSimpleName();

    private DatabaseHelper mDatabaseHelper;
    private Picture mPicture;

    @InjectView(R.id.imageView)
    ImageView mImageView;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.likesTextView)
    TextView mLikesTextView;
    @InjectView(R.id.viewsTextView)
    TextView mViewsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_full_picture);

        ButterKnife.inject(this);

        // Get access to the database
        mDatabaseHelper = DatabaseHelper.getInstance(this);

        // Hide action bar and progress bar
        getSupportActionBar().hide();
        mProgressBar.setVisibility(View.GONE);

        // Get photo id from intent
        String uniqueId = getIntent().getStringExtra("uniqueId");

        // Show the loading progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        // Database interactions
        mPicture = mDatabaseHelper.getFeedPicture(uniqueId);    // get image from feed table
        if (!mDatabaseHelper.isPictureViewed(mPicture))
            mDatabaseHelper.viewPicture(mPicture);    // update views in table

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
                .load(mPicture.getFullUrl())
                .resize(1024, 0)
                .onlyScaleDown()
                .into(target);  // load image from url into imageview

        // Displays the statistics on the screen and fetches the statistics from the server
        mLikesTextView.setText("Likes: " + mPicture.getLikes());
        mViewsTextView.setText("Views: " + mPicture.getViews());

        setPictureSwipeGestures();

        if (NetworkUtilities.isNetworkAvailable(this))
            fetchPictureStatisticsFromServer();
    }

    /**
     * Handles the swipe / tap gestures.
     */
    private void setPictureSwipeGestures() {
        mImageView.setOnTouchListener(new OnHorizontalSwipeListener(this) {

            /**
             * Swiping left likes the photo then goes back to the feed.
             */
            @Override
            public void onSwipeLeft() {
                if (!mDatabaseHelper.isPictureLiked(mPicture)) {
                    mDatabaseHelper.likePicture(mPicture);
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
                mDatabaseHelper.hidePicture(mPicture);
                setResult(ActionCodes.HIDE_PICTURE);
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
        String url = getString(R.string.server_like) + mPicture.getUniqueId();
        HerokuRestClient.get(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    FileUtilities.logResults(FeedFullPictureActivity.this, LOG_TAG, "Liked pic!");
                    String jsonData = new String(responseBody);
                    JSONObject jsonObject = new JSONObject(jsonData);
                    mPicture.setLikes(jsonObject.getInt(getString(R.string.json_likes)));
                    mPicture.setViews(jsonObject.getInt(getString(R.string.json_views)));
                    mDatabaseHelper.updateStatisticsFromImage(mPicture);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                FileUtilities.logResults(FeedFullPictureActivity.this, LOG_TAG, "Failed to like pic!");
            }
        });
    }

    /**
     * Dislikes the picture on the server
     */
    public void dislikePicture() {
        String url = getString(R.string.server_dislike) + mPicture.getUniqueId();
        HerokuRestClient.get(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    FileUtilities.logResults(FeedFullPictureActivity.this, LOG_TAG, "Disliked pic!");
                    String jsonData = new String(responseBody);
                    JSONObject jsonObject = new JSONObject(jsonData);
                    mPicture.setLikes(jsonObject.getInt(getString(R.string.json_likes)));
                    mPicture.setViews(jsonObject.getInt(getString(R.string.json_views)));
                    mDatabaseHelper.updateStatisticsFromImage(mPicture);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                FileUtilities.logResults(FeedFullPictureActivity.this, LOG_TAG, "Failed to dislike pic!");
            }
        });
    }

    /**
     * Gets the image statistics from the server and updates
     * the internal database with the new statistics. Also
     * displays the new statistics on the screen.
     */
    public void fetchPictureStatisticsFromServer() {
        HerokuRestClient.get(mPicture.getUniqueId(), null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String jsonData = new String(responseBody);
                    JSONObject jsonObject = new JSONObject(jsonData);
                    mPicture.setViews(jsonObject.getInt("view"));
//                    mImage.setViews(jsonObject.getInt(getString(R.string.json_views)));
                    mPicture.setLikes(jsonObject.getInt(getString(R.string.json_likes)));

                    mDatabaseHelper.updateStatisticsFromImage(mPicture);

                    mLikesTextView.setText("Likes: " + mPicture.getLikes());
                    mViewsTextView.setText("Views: " + mPicture.getViews());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                FileUtilities.logResults(FeedFullPictureActivity.this, LOG_TAG, "Request for statistics failed");
            }
        });
    }

    public void viewImage() {
        String url = "view/" + mPicture.getUniqueId();
        HerokuRestClient.get(url, null, null);
    }

}
