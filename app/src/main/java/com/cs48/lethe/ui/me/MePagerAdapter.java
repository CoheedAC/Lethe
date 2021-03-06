package com.cs48.lethe.ui.me;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
 * A custom pager adapter for the full-screen view
 * of the me pictures that allows the slideshow effect
 */
public class MePagerAdapter extends PagerAdapter {

    // Logcat tag
    public final static String TAG = MePagerAdapter.class.getSimpleName();

    // Instance variables
    private MeFullScreenActivity mMeFullScreenActivity;
    private List<Picture> mPictureList;
    private LayoutInflater mLayoutInflater;
    private DatabaseHelper mDatabaseHelper;

    // Initializations of UI elements
    @InjectView(R.id.imageView)
    PinchToZoomImageView mImageView;
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

    /**
     * Constructor that gets the posted pictures from the database
     *
     * @param context Interface to global information about an application environment
     */
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
        return view == object;
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
        // Inflates the view from the full screen layout
        View itemView = mLayoutInflater.inflate(R.layout.layout_fullscreen, container, false);

        // Injects the UI elements into the activity
        ButterKnife.inject(this, itemView);

        // Picture at the current position
        Picture picture = mPictureList.get(position);

        // Shows the statistics
        mLikesTextView.setText(picture.getLikes() + "");
        mViewsTextView.setText(picture.getViews() + "");

        // Gets the updated stats from the server
        fetchPictureStatisticsFromServer(picture);

        // Shows the ciy name
        LatLng latLng = new LatLng(picture.getLatitude(), picture.getLongitude());
        Geocoder geocoder = new Geocoder(mMeFullScreenActivity);
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address address = addressList.get(0);
            AutofitHelper.create(mCityTextView);
            mCityTextView.setText(address.getLocality());
        } catch (IOException | IndexOutOfBoundsException e) {
            mCityTextView.setVisibility(View.GONE);
        }

        // Set up on click listeners
        mImageView.setOnClickListener(new OnPictureClickListener());
        mDeleteButton.setOnClickListener(new OnDeleteButtonClickListener(position));
        mSaveButton.setOnClickListener(new OnSaveButtonClickListener(position));

        // Displays the picture
        Picasso.with(mMeFullScreenActivity)
                .load(picture.getFile())
                .resize(PictureUtilities.MAX_FULL_WIDTH, 0)
                .onlyScaleDown()
                .rotate(picture.getOrientation())
                .into(mImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    /**
                     * Alerts the user that it could not load the picture successfully
                     */
                    @Override
                    public void onError() {
                        try {
                            new OperationFailedAlertDialog().show(mMeFullScreenActivity.getFragmentManager(), TAG);
                        } catch (IllegalStateException e) {
                            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                        }
                    }
                });

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
        container.removeView((View) object);
        Picasso.with(mMeFullScreenActivity)
                .cancelRequest(mImageView);
    }

    /**
     * Gets the image statistics from the server and updates
     * the internal database with the new statistics. Also
     * displays the new statistics on the screen.
     */
    public void fetchPictureStatisticsFromServer(final Picture picture) {
        String url = mMeFullScreenActivity.getString(R.string.server_statistics) + picture.getUniqueId();
        HerokuRestClient.get(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String jsonData = new String(responseBody);
                    JSONObject jsonObject = new JSONObject(jsonData);
                    picture.setViews(jsonObject.getInt(mMeFullScreenActivity.getString(R.string.json_views)));
                    picture.setLikes(jsonObject.getInt(mMeFullScreenActivity.getString(R.string.json_likes)));

                    mDatabaseHelper.updateDatabaseFromPicture(picture);

                    mLikesTextView.setText(picture.getLikes() + "");
                    mViewsTextView.setText(picture.getViews() + "");
                } catch (JSONException e) {
                    Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, "Request for statistics failed");
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
            String url = mMeFullScreenActivity.getString(R.string.server_delete) + mPictureList.get(position).getUniqueId();
            HerokuRestClient.get(url, null, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    deletePicture();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    if (statusCode == 400) {
                        deletePicture();
                    }else {
                        try {
                            new OperationFailedAlertDialog().show(mMeFullScreenActivity.getFragmentManager(), TAG);
                        } catch (IllegalStateException e) {
                            Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                        }
                    }
                }

                private void deletePicture() {
                    mDatabaseHelper.deletePictureFromDatabase(mPictureList.get(position));
                    Toast toast = Toast.makeText(mMeFullScreenActivity, "Deleted picture!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
                    toast.show();
                    mMeFullScreenActivity.finish();
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
                String message = (FileUtilities.savePictureForSharing(mMeFullScreenActivity, mPictureList.get(position))) ?
                    "Saved picture to shared storage." : "Picture already exists in shared storage";
                Toast toast = Toast.makeText(mMeFullScreenActivity, message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
                toast.show();
            } catch (IOException e) {
                Toast toast = Toast.makeText(mMeFullScreenActivity, "Cannot copy to shared storage.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
                toast.show();
            }
        }
    }

}