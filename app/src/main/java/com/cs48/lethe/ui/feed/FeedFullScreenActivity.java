package com.cs48.lethe.ui.feed;

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
import com.cs48.lethe.ui.alertdialogs.OperationFailedAlertDialog;
import com.cs48.lethe.ui.alertdialogs.PictureAlreadyLikedAlertDialog;
import com.cs48.lethe.ui.miscellaneous.OnHorizontalSwipeListener;
import com.cs48.lethe.ui.miscellaneous.PinchToZoomImageView;
import com.cs48.lethe.utils.NetworkUtilities;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.PictureUtilities;
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

    // Logcat tag
    public static final String TAG = FeedFullScreenActivity.class.getSimpleName();

    // Instance variables
    private DatabaseHelper mDatabaseHelper;
    private Picture mPicture;

    // Initializations of UI elements
    @InjectView(R.id.imageView)
    PinchToZoomImageView mImageView;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.likesTextView)
    TextView mLikesTextView;
    @InjectView(R.id.viewsTextView)
    TextView mViewsTextView;
    @InjectView(R.id.buttonsLinearLayout)
    LinearLayout mButtonsLinearLayout;

    /**
     * Called when the activity is starting. This is where most initialization should go:
     * calling setContentView(int) to inflate the activity's UI, using findViewById(int)
     * to programmatically interact with widgets in the UI, and initializing other variables
     * that need to be set.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets the view to the full screen layout
        setContentView(R.layout.layout_fullscreen);

        // Injects the UI elements into the activity
        ButterKnife.inject(this);

        // Get access to the database
        mDatabaseHelper = DatabaseHelper.getInstance(this);

        // Hide action bar and progress bar
        getSupportActionBar().hide();
        mButtonsLinearLayout.setVisibility(View.GONE);

        // Get photo id passed in from the intent
        String uniqueId = getIntent().getStringExtra(getString(R.string.data_uniqueId));

        // Show the loading progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        // Get picture from feed table
        mPicture = mDatabaseHelper.getFeedPicture(uniqueId);

        // Represents an arbitrary listener for image loading.
        final Target target = new Target() {
            /**
             * Callback when an image has been successfully loaded.
             *
             * @param bitmap The bitmap of the picture
             * @param from Describes where the image was loaded from
             */
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // Hides the loading progress bar
                mProgressBar.setVisibility(View.GONE);

                // Displays the picture
                mImageView.setImageBitmap(bitmap);
            }

            /**
             * Callback indicating the image could not be successfully loaded.
             *
             * @param errorDrawable Drawable that was set from
             *                      the Picasso call
             */
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                // Hides the loading progress bar
                mProgressBar.setVisibility(View.GONE);

                // Displays operation failed alert dialog
                try {
                    new OperationFailedAlertDialog().show(getFragmentManager(), TAG);
                } catch (IllegalStateException e) {
                    Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            /**
             * Callback invoked right before your request is submitted.
             *
             * @param placeHolderDrawable Placeholder drawable that was
             *                            set from the Picasso call
             */
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        // Makes a strong reference to the target
        mImageView.setTag(target);

        // If the picture file doesn't exist, then the picture
        // is loaded from a URL to the imageview
        if (mPicture.getFile() == null) {
            Picasso.with(this)
                    .load(mPicture.getFullUrl())
                    .resize(PictureUtilities.MAX_FULL_WIDTH, 0)
                    .onlyScaleDown()
                    .into(target);
        }else {
            // Else the picture is a file, so load the picture
            // from the file to the imageview
            Picasso.with(this)
                    .load(mPicture.getFile())
                    .resize(PictureUtilities.MAX_FULL_WIDTH, 0)
                    .onlyScaleDown()
                    .into(target);
        }

        // Sets up the swipe gestures on the imageview
        mImageView.setOnTouchListener(new OnSwipeListener(this));

        // Update view count if picture hasn't already been viewed
        if (!mDatabaseHelper.isPictureViewed(mPicture))
            viewPicture();
        // Fetches the statistics from the server if there is internet
        // and the picture has already been viewed before
        else {
            // Displays the database statistics on the screen
            mLikesTextView.setText("Likes: " + mPicture.getLikes());
            mViewsTextView.setText("Views: " + mPicture.getViews());

            // If there is internet, then fetch statistics
            // from the server
            if (NetworkUtilities.isNetworkAvailable(this))
                fetchPictureStatisticsFromServer();
        }
    }

    /**
     * Likes the picture by incrementing the like count
     * on the internal database and also on the server
     */
    public void likePicture() {
        mDatabaseHelper.likePicture(mPicture);
        String url = getString(R.string.server_like) + mPicture.getUniqueId();
        HerokuRestClient.get(url, null, mStatisticsResponseHandler);
    }

    /**
     * Dislikes the picture by decrementing the like count
     * on the internal database and also on the server and
     * hides it from the user's view on the grid
     */
    public void hidePicture() {
        mDatabaseHelper.hidePicture(mPicture);
        String url = getString(R.string.server_dislike) + mPicture.getUniqueId();
        HerokuRestClient.get(url, null, mStatisticsResponseHandler);
    }

    public void viewPicture() {
        // Update view count in the database
        mDatabaseHelper.viewPicture(mPicture);

        // Displays the statistics on the screen
        mLikesTextView.setText("Likes: " + mPicture.getLikes());
        mViewsTextView.setText("Views: " + mPicture.getViews());
    }

    /**
     * Gets the image statistics from the server and updates
     * the internal database with the new statistics. Also
     * displays the new statistics on the screen.
     */
    public void fetchPictureStatisticsFromServer() {
        String url = getString(R.string.server_statistics) + mPicture.getUniqueId();
        HerokuRestClient.get(url, null, mStatisticsResponseHandler);
    }

    /**
     * A callback to be invoked when a touch event is dispatched to this view.
     * The callback will be invoked before the touch event is given to the view.
     *
     * This handles the swipe and tap gestures.
     */
    private class OnSwipeListener extends OnHorizontalSwipeListener {

        /**
         * Constructor for the Swipe Listener
         *
         * @param context Interface to global information about an application environment
         */
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
                new PictureAlreadyLikedAlertDialog().show(getFragmentManager(), TAG);
        }

        /**
         * Swiping right hides the photo from the feed and dislikes
         * it on the server, then returns to the feed.
         */
        @Override
        public void onSwipeRight() {
            hidePicture();
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
    private AsyncHttpResponseHandler mStatisticsResponseHandler = new AsyncHttpResponseHandler() {
        /**
         * Fired when a request returns successfully. This gets the response
         * from the server and and updates the UI and database based upon
         * the response.
         *
         * @param statusCode   the status code of the response
         * @param headers      return headers, if any
         * @param responseBody the body of the HTTP response from the server
         */
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            try {
                String jsonData = new String(responseBody);
                JSONObject jsonObject = new JSONObject(jsonData);

                // Updates the picture statistics from the response
                mPicture.setViews(jsonObject.getInt(getString(R.string.json_views)));
                mPicture.setLikes(jsonObject.getInt(getString(R.string.json_likes)));

                // Updates the database with the new statistics
                mDatabaseHelper.updateDatabaseFromPicture(mPicture);

                // Updates the text views to show the new statistics
                mLikesTextView.setText("Likes: " + mPicture.getLikes());
                mViewsTextView.setText("Views: " + mPicture.getViews());
            } catch (JSONException e) {
                Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            }
        }

        /**
         * Fired when a request fails to complete.
         *
         * @param statusCode   return HTTP status code
         * @param headers      return headers, if any
         * @param responseBody the response body, if any
         * @param error        the underlying cause of the failure
         */
        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.d(TAG, "Operation failed");
        }
    };
}
