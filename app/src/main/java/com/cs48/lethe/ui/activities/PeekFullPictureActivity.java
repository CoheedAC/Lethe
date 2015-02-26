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
import com.cs48.lethe.networking.HerokuRestClient;
import com.cs48.lethe.ui.dialogs.OperationFailedDialog;
import com.cs48.lethe.ui.view_helpers.OnHorizontalSwipeListener;
import com.cs48.lethe.utils.FileUtilities;
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

public class PeekFullPictureActivity extends ActionBarActivity {

    public static final String LOG_TAG = FeedFullPictureActivity.class.getSimpleName();

//    private List<Picture> mPictureList;
    private Picture mPicture;
//    private int mPosition;

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

        // Hide action bar and progress bar
        getSupportActionBar().hide();
        mProgressBar.setVisibility(View.GONE);

        // Get picture from intent
        mPicture = (Picture) getIntent().getSerializableExtra(getString(R.string.data_picture));

        // Show the loading progress bar
        mProgressBar.setVisibility(View.VISIBLE);

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
            }

            /**
             * Swiping right hides the photo from the feed and dislikes
             * it on the server, then returns to the feed.
             */
            @Override
            public void onSwipeRight() {
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

                    mLikesTextView.setText("Likes: " + mPicture.getLikes());
                    mViewsTextView.setText("Views: " + mPicture.getViews());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                FileUtilities.logResults(PeekFullPictureActivity.this, LOG_TAG, "Request for statistics failed");
            }
        });
    }

    public void viewImage() {
        String url = "view/" + mPicture.getUniqueId();
        HerokuRestClient.get(url, null, null);
    }
}
