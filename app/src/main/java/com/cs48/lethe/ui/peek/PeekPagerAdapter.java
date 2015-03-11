package com.cs48.lethe.ui.peek;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.ui.miscellaneous.PinchToZoomImageView;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.PictureUtilities;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.grantland.widget.AutofitHelper;

/**
 * Created by maxkohne on 2/26/15.
 */
public class PeekPagerAdapter extends PagerAdapter {

    public final static String TAG = PeekPagerAdapter.class.getSimpleName();

    private PeekFullScreenActivity mPeekFullScreenActivity;
    private List<Picture> mPictureList;
    private LayoutInflater mLayoutInflater;
    private DatabaseHelper mDatabaseHelper;

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

    public PeekPagerAdapter(Context context) {
        mPeekFullScreenActivity = (PeekFullScreenActivity) context;
        mDatabaseHelper = DatabaseHelper.getInstance(mPeekFullScreenActivity);
        mLayoutInflater = (LayoutInflater) mPeekFullScreenActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPictureList = mDatabaseHelper.getPeekPictures();
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
    public View instantiateItem(ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.layout_fullscreen, container, false);

        ButterKnife.inject(this, itemView);

        final Picture picture = mPictureList.get(position);
        LatLng latLng = new LatLng(picture.getLatitude(), picture.getLongitude());
        Geocoder geocoder = new Geocoder(mPeekFullScreenActivity);
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address address = addressList.get(0);
            AutofitHelper.create(mCityTextView);
            mCityTextView.setText(address.getLocality());
        } catch (IOException | IndexOutOfBoundsException e) {
            mCityTextView.setVisibility(View.GONE);
        }

        mLikesTextView.setText(mPictureList.get(position).getLikes() + "");
        mViewsTextView.setText(mPictureList.get(position).getViews() + "");

        mButtonsLinearLayout.setVisibility(View.GONE);

        // Set up on click listeners
        mImageView.setOnClickListener(new OnPictureClickListener());

        Picasso.with(mPeekFullScreenActivity)
                .load(mPictureList.get(position).getFullUrl())
                .resize(PictureUtilities.MAX_FULL_WIDTH, 0)
                .onlyScaleDown()
                .rotate(mPictureList.get(position).getOrientation())
                .into(mImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        mDatabaseHelper.deletePictureFromFeedTable(picture);
                        mDatabaseHelper.deletePictureFromPeekTable(picture);
                        try {
                            new AlertDialog.Builder(mPeekFullScreenActivity)
                                    .setTitle(mPeekFullScreenActivity.getString(R.string.alert_title_picture_unavailable))
                                    .setMessage(mPeekFullScreenActivity.getString(R.string.alert_message_picture_unavailable))
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            mPeekFullScreenActivity.finish();
                                        }
                                    })
                                    .show();
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
    }

    /**
     * A callback to be invoked when a view is clicked.
     */
    class OnPictureClickListener implements View.OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            mPeekFullScreenActivity.finish();
        }
    }

}