package com.cs48.lethe.ui.me;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.ui.alertdialogs.OperationFailedAlertDialog;
import com.cs48.lethe.ui.miscellaneous.PinchToZoomImageView;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.HerokuRestClient;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.PictureUtilities;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.grantland.widget.AutofitHelper;

/**
 * Created by maxkohne on 2/26/15.
 */
public class MePagerAdapter extends PagerAdapter {

    public final static String LOG_TAG = MePagerAdapter.class.getSimpleName();

    private MeFullScreenActivity mMeFullScreenActivity;
    private List<Picture> mPictureList;
    private LayoutInflater mLayoutInflater;
    private DatabaseHelper mDatabaseHelper;
    private Target mTarget;

    @InjectView(R.id.imageView)
    PinchToZoomImageView mImageView;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.likesTextView)
    TextView mLikesTextView;
    @InjectView(R.id.viewsTextView)
    TextView mViewsTextView;
    @InjectView(R.id.saveButton)
    ImageButton mSaveButton;
    @InjectView(R.id.deleteButton)
    ImageButton mDeleteButton;
    @InjectView(R.id.cityTextView)
    TextView mCityTextView;

    public MePagerAdapter(Context context) {
        mMeFullScreenActivity = (MeFullScreenActivity) context;
        mDatabaseHelper = DatabaseHelper.getInstance(mMeFullScreenActivity);
        mLayoutInflater = (LayoutInflater) mMeFullScreenActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPictureList = mDatabaseHelper.getMePictures();
    }

    /**
     * @return Return the number of views available.
     */
    @Override
    public int getCount() {
        return this.mPictureList.size();
    }

    /**
     * Determines whether a page View is associated with a specific
     * key object as returned by instantiateItem(ViewGroup, int).
     * This method is required for a PagerAdapter to function properly.
     *
     * @param view Page View to check for association with object
     * @param object Object to check for association with view
     *
     * @return True if view is associated with the key object object.
     *         False otherwise.
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    /**
     * Create the page for the given position. The adapter is responsible
     * for adding the view to the container given here.
     *
     * @param container The containing View in which the page will be shown.
     * @param position The page position to be instantiated.
     *
     * @return Returns a view representing the new page.
     */
    @Override
    public View instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.layout_fullscreen, container, false);

        ButterKnife.inject(this, itemView);

        AutofitHelper.create(mCityTextView);
        mCityTextView.setVisibility(View.VISIBLE);

        Picture picture = mPictureList.get(position);
        LatLng latLng = new LatLng(picture.getLatitude(), picture.getLongitude());
        Geocoder geocoder = new Geocoder(mMeFullScreenActivity);
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address address = addressList.get(0);
            mCityTextView.setText(address.getLocality());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            mCityTextView.setVisibility(View.GONE);
        }

        mLikesTextView.setText(picture.getLikes() + "");
        mViewsTextView.setText(picture.getViews() + "");

        // Set up on click listeners
        mImageView.setOnClickListener(new OnPictureClickListener());
        mDeleteButton.setOnClickListener(new OnDeleteButtonClickListener(position));
        mSaveButton.setOnClickListener(new OnSaveButtonClickListener(position));


        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mProgressBar.setVisibility(View.GONE);
                mImageView.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                mProgressBar.setVisibility(View.GONE);
                try {
                    new OperationFailedAlertDialog().show(mMeFullScreenActivity.getFragmentManager(), LOG_TAG);
                } catch (IllegalStateException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        };

        // Display full image
        mImageView.setTag(mTarget);

        Picasso.with(mMeFullScreenActivity)
                .load(picture.getFile())
                .resize(PictureUtilities.MAX_FULL_WIDTH, 0)
                .onlyScaleDown()
                .rotate(mPictureList.get(position).getOrientation())
                .into(mTarget);

        fetchPictureStatisticsFromServer(position);

        container.addView(itemView);

        return itemView;
    }

    /**
     * Remove a page for the given position. The adapter is
     * responsible for removing the view from its container.
     *
     * @param container The containing View from which the page will be removed.
     * @param position The page position to be removed.
     * @param object The same object that was returned by instantiateItem(View, int).
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
        Picasso.with(mMeFullScreenActivity)
                .cancelRequest(mTarget);
    }

    /**
     * Gets the image statistics from the server and updates
     * the internal database with the new statistics. Also
     * displays the new statistics on the screen.
     */
    public void fetchPictureStatisticsFromServer(final int position) {
        String url = mMeFullScreenActivity.getString(R.string.server_statistics) + mPictureList.get(position).getUniqueId();
        HerokuRestClient.get(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String jsonData = new String(responseBody);
                    JSONObject jsonObject = new JSONObject(jsonData);
                    mPictureList.get(position).setViews(jsonObject.getInt(mMeFullScreenActivity.getString(R.string.json_views)));
                    mPictureList.get(position).setLikes(jsonObject.getInt(mMeFullScreenActivity.getString(R.string.json_likes)));

                    mDatabaseHelper.updateDatabaseFromPicture(mPictureList.get(position));

                    mLikesTextView.setText(mPictureList.get(position).getLikes() + "");
                    mViewsTextView.setText(mPictureList.get(position).getViews() + "");
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(LOG_TAG, "Request for statistics failed");
            }
        });
    }

    /**
     * A callback to be invoked when a view is clicked.
     */
    private class OnPictureClickListener implements View.OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            mMeFullScreenActivity.finish();
        }
    }

    /**
     * A callback to be invoked when a view is clicked.
     */
    private class OnDeleteButtonClickListener implements View.OnClickListener {

        int position;

        public OnDeleteButtonClickListener(int position) {
            this.position = position;
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            mProgressBar.setVisibility(View.VISIBLE);
            String url = mMeFullScreenActivity.getString(R.string.server_delete) + mPictureList.get(position).getUniqueId();
            HerokuRestClient.get(url, null, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    mProgressBar.setVisibility(View.GONE);
                    mDatabaseHelper.deletePictureFromDatabase(mPictureList.get(position));
                    mMeFullScreenActivity.finish();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    mProgressBar.setVisibility(View.GONE);
                    try {
                        new OperationFailedAlertDialog().show(mMeFullScreenActivity.getFragmentManager(), LOG_TAG);
                    }catch (IllegalStateException e) {
                        Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                    }
                }
            });
        }
    }

    /**
     * A callback to be invoked when a view is clicked.
     */
    private class OnSaveButtonClickListener implements View.OnClickListener {

        int position;

        public OnSaveButtonClickListener(int position) {
            this.position = position;
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            try {
                if (FileUtilities.savePictureForSharing(mMeFullScreenActivity, mPictureList.get(position)))
                    Toast.makeText(mMeFullScreenActivity, "Saved picture to shared storage.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mMeFullScreenActivity, "Picture already exists in shared storage", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(mMeFullScreenActivity, "Cannot copy to shared storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}