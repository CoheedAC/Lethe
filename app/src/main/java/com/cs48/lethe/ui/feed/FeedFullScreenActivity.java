package com.cs48.lethe.ui.feed;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.ui.miscellaneous.OnHorizontalSwipeListener;
import com.cs48.lethe.ui.miscellaneous.PinchToZoomImageView;
import com.cs48.lethe.utils.HerokuRestClient;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.PictureUtilities;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.grantland.widget.AutofitHelper;

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
    @InjectView(R.id.likesTextView)
    TextView mLikesTextView;
    @InjectView(R.id.viewsTextView)
    TextView mViewsTextView;
    @InjectView(R.id.buttonsLinearLayout)
    LinearLayout mButtonsLinearLayout;
    @InjectView(R.id.cityTextView)
    TextView mCityTextView;

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

        // Get picture from feed table
        mPicture = mDatabaseHelper.getFeedPicture(uniqueId);

        // Update view count if picture hasn't already been viewed
        if (!mDatabaseHelper.isPictureViewed(mPicture))
            viewPicture();
        // Displays the database statistics on the screen
        mLikesTextView.setText(mPicture.getLikes() + "");
        mViewsTextView.setText(mPicture.getViews() + "");

        LatLng latLng = new LatLng(mPicture.getLatitude(), mPicture.getLongitude());
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address address = addressList.get(0);
            AutofitHelper.create(mCityTextView);
            mCityTextView.setText(address.getLocality());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            mCityTextView.setVisibility(View.GONE);
        }

        // If the picture file doesn't exist, then the picture
        // is loaded from a URL to the imageview
        if (mPicture.getFile() == null) {
            Picasso.with(this)
                    .load(mPicture.getFullUrl())
                    .resize(PictureUtilities.MAX_FULL_WIDTH, 0)
                    .onlyScaleDown()
                    .rotate(mPicture.getOrientation())
                    .into(mImageView, new PictureCallBack());
        } else {
            // Else the picture is a file, so load the picture
            // from the file to the imageview
            Picasso.with(this)
                    .load(mPicture.getFile())
                    .resize(PictureUtilities.MAX_FULL_WIDTH, 0)
                    .onlyScaleDown()
                    .rotate(mPicture.getOrientation())
                    .into(mImageView, new PictureCallBack());
        }

        // Sets up the swipe gestures on the imageview
        mImageView.setOnTouchListener(new OnSwipeListener(this));
    }

    /**
     * Likes the picture by incrementing the like count
     * on the internal database and also on the server
     */
    public void likePicture() {
        mDatabaseHelper.likePicture(mPicture);
        String url = getString(R.string.server_like) + mPicture.getUniqueId();
        HerokuRestClient.get(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            }
        });
    }

    /**
     * Dislikes the picture by decrementing the like count
     * on the internal database and also on the server and
     * hides it from the user's view on the grid
     */
    public void hidePicture() {
        mDatabaseHelper.hidePicture(mPicture);
        String url = getString(R.string.server_dislike) + mPicture.getUniqueId();
        HerokuRestClient.get(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            }
        });
    }

    public void viewPicture() {
        // Update view count in the database
        mDatabaseHelper.viewPicture(mPicture);
        HerokuRestClient.get(mPicture.getUniqueId(), null, new AsyncHttpResponseHandler() {
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
                    mLikesTextView.setText(mPicture.getLikes() + "");
                    mViewsTextView.setText(mPicture.getViews() + "");
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
                Log.d(TAG, "code = " + statusCode);
            }
        });
    }

    /**
     * A callback to be invoked when a touch event is dispatched to this view.
     * The callback will be invoked before the touch event is given to the view.
     * <p/>
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
         * Swiping left hides the photo from the feed and dislikes
         * it on the server, then returns to the feed.
         */
        @Override
        public void onSwipeLeft() {
            Toast toast = Toast.makeText(FeedFullScreenActivity.this, "Picture is now hidden!", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            hidePicture();
            finish();
        }

        /**
         * Swiping right likes the photo then goes back to the feed.
         */
        @Override
        public void onSwipeRight() {
            if (!mDatabaseHelper.isPictureLiked(mPicture)) {
                likePicture();
                Toast toast = Toast.makeText(FeedFullScreenActivity.this, "Liked picture!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                finish();
            } else {
                Toast toast = Toast.makeText(FeedFullScreenActivity.this, "You have already liked this picture!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        }

        /**
         * Tapping anywhere on the screen goes back to the feed.
         */
        @Override
        public void onSingleTap() {
            finish();
        }
    }

    private class PictureCallBack implements Callback {
        @Override
        public void onSuccess() {
        }

        @Override
        public void onError() {
            mDatabaseHelper.deletePictureFromFeedTable(mPicture);
            mDatabaseHelper.deletePictureFromPeekTable(mPicture);
            try {
                new AlertDialog.Builder(FeedFullScreenActivity.this)
                        .setTitle("Picture No Longer Available")
                        .setMessage("The picture has either expired or been deleted.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            } catch (IllegalStateException e) {
                Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
            }
        }
    }
}
