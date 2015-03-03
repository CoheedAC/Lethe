package com.cs48.lethe.ui.peek;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.ui.alertdialogs.OperationFailedDialog;
import com.cs48.lethe.ui.miscellaneous.PinchToZoomImageView;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.PictureUtilities;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

/**
 * Created by maxkohne on 2/26/15.
 */
public class PeekPagerAdapter extends PagerAdapter {

    public final static String LOG_TAG = PeekPagerAdapter.class.getSimpleName();

    private List<Picture> mPictureList;
    private LayoutInflater mLayoutInflater;
    private DatabaseHelper mDatabaseHelper;
    private PeekFullScreenActivity mPeekFullScreenActivity;

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

        final PinchToZoomImageView imageView = (PinchToZoomImageView) itemView.findViewById(R.id.imageView);
        final ProgressBar progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        TextView likesTextView = (TextView) itemView.findViewById(R.id.likesTextView);
        TextView viewsTextView = (TextView) itemView.findViewById(R.id.viewsTextView);
        LinearLayout linearLayout = (LinearLayout) itemView.findViewById(R.id.buttonsLinearLayout);

        likesTextView.setText("Likes: " + mPictureList.get(position).getLikes());
        viewsTextView.setText("Views: " + mPictureList.get(position).getViews());

        linearLayout.setVisibility(View.GONE);

        // Display full image
        final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                progressBar.setVisibility(View.GONE);
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                progressBar.setVisibility(View.GONE);
                try {
                    new OperationFailedDialog().show(mPeekFullScreenActivity.getFragmentManager(), LOG_TAG);
                } catch (IllegalStateException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                progressBar.setVisibility(View.VISIBLE);
            }
        };
        imageView.setTag(target);

        // Set up on click listeners
        imageView.setOnClickListener(new OnPictureClickListener());

        Picasso.with(mPeekFullScreenActivity)
                .load(mPictureList.get(position).getFullUrl())
                .resize(PictureUtilities.MAX_FULL_WIDTH, 0)
                .onlyScaleDown()
                .into(target);

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