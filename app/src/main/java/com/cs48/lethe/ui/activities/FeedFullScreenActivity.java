package com.cs48.lethe.ui.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.networking.HerokuRestClient;
import com.cs48.lethe.ui.dialogs.AlreadyLikedImageDialog;
import com.cs48.lethe.ui.dialogs.OperationFailedDialog;
import com.cs48.lethe.ui.view_helpers.OnHorizontalSwipeListener;
import com.cs48.lethe.ui.view_helpers.TouchImageView;
import com.cs48.lethe.utils.ActionCodes;
import com.cs48.lethe.utils.NetworkUtilities;
import com.cs48.lethe.utils.Picture;
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
public class FeedFullScreenActivity extends ActionBarActivity {

    public static final String LOG_TAG = FeedFullScreenActivity.class.getSimpleName();

    private DatabaseHelper mDatabaseHelper;
    private Picture mPicture;

    @InjectView(R.id.imageView)
    TouchImageView mImageView;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.likesTextView)
    TextView mLikesTextView;
    @InjectView(R.id.viewsTextView)
    TextView mViewsTextView;
    @InjectView(R.id.buttonsLinearLayout)
    LinearLayout mButtonsLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_fullscreen);

        ButterKnife.inject(this);

        // Get access to the database
        mDatabaseHelper = DatabaseHelper.getInstance(this);

        // Hide action bar and progress bar
        getSupportActionBar().hide();
        mButtonsLinearLayout.setVisibility(View.GONE);

        // Get photo id from intent
        String uniqueId = getIntent().getStringExtra(getString(R.string.data_uniqueId));

        // Show the loading progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        // Database interactions
        mPicture = mDatabaseHelper.getFeedPicture(uniqueId);    // get image from feed table
        if (!mDatabaseHelper.isPictureViewed(mPicture))
            mDatabaseHelper.viewPicture(mPicture);    // update views in table

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
                .into(target);

        // Displays the statistics on the screen and fetches the statistics from the server
        mLikesTextView.setText("Likes: " + mPicture.getLikes());
        mViewsTextView.setText("Views: " + mPicture.getViews());

        mImageView.setOnTouchListener(new OnSwipeListener(this));

        if (NetworkUtilities.isNetworkAvailable(this))
            fetchPictureStatisticsFromServer();
    }

    /**
     * Likes the picture on the server
     */
    public void likePicture() {
        mDatabaseHelper.likePicture(mPicture);
        String url = getString(R.string.server_like) + mPicture.getUniqueId();
        HerokuRestClient.get(url, null, new StatisticsResponseHandler());
    }

    /**
     * Dislikes the picture on the server
     */
    public void dislikePicture() {
        mDatabaseHelper.hidePicture(mPicture);
        String url = getString(R.string.server_dislike) + mPicture.getUniqueId();
        HerokuRestClient.get(url, null, new StatisticsResponseHandler());
    }

    /**
     * Gets the image statistics from the server and updates
     * the internal database with the new statistics. Also
     * displays the new statistics on the screen.
     */
    public void fetchPictureStatisticsFromServer() {
        HerokuRestClient.get(mPicture.getUniqueId(), null, new StatisticsResponseHandler());
    }

    /**
     * Handles the swipe / tap gestures.
     */
    class OnSwipeListener extends OnHorizontalSwipeListener {

        public OnSwipeListener(Context context) {
            super(context);
        }

        /**
         * Swiping left likes the photo then goes back to the feed.
         */
        @Override
        public void onSwipeLeft() {
            if (!mDatabaseHelper.isPictureLiked(mPicture)) {
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
            dislikePicture();
            setResult(ActionCodes.HIDE_PICTURE);
            finish();
        }

        /**
         * Tapping anywhere on the screen goes back to the feed.
         */
        @Override
        public void onSingleTap() {
            finish();
        }
    }

    /**
     * Handles the response from server requests
     */
    class StatisticsResponseHandler extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            try {
                String jsonData = new String(responseBody);
                JSONObject jsonObject = new JSONObject(jsonData);
                mPicture.setViews(jsonObject.getInt(getString(R.string.json_views)));
                mPicture.setLikes(jsonObject.getInt(getString(R.string.json_likes)));
                mDatabaseHelper.updateDatabaseFromPicture(mPicture);
                mLikesTextView.setText("Likes: " + mPicture.getLikes());
                mViewsTextView.setText("Views: " + mPicture.getViews());
                Log.d(LOG_TAG, "Operation succeeded");
            } catch (JSONException e) {
                Log.d(LOG_TAG, "Operation caught by exception");
                Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.d(LOG_TAG, "Operation failed");
        }
    }

}
