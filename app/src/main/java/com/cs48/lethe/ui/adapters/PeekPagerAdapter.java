package com.cs48.lethe.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.ui.activities.PeekFullScreenActivity;
import com.cs48.lethe.ui.dialogs.OperationFailedDialog;
import com.cs48.lethe.ui.view_helpers.TouchImageView;
import com.cs48.lethe.utils.Picture;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

/**
 * Created by maxkohne on 2/26/15.
 */
public class PeekPagerAdapter extends PagerAdapter {

    public final static String LOG_TAG = PeekPagerAdapter.class.getSimpleName();

    private Context mContext;
    private List<Picture> mPictureList;
    private LayoutInflater mLayoutInflater;
    private DatabaseHelper mDatabaseHelper;

    public PeekPagerAdapter(Context context) {
        mContext = context;
        mDatabaseHelper = DatabaseHelper.getInstance(mContext);
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPictureList = mDatabaseHelper.getPeekPictures();
    }

    /**
     * Returns the number of items in the fullscreen slideshow
     */
    @Override
    public int getCount() {
        return this.mPictureList.size();
    }

    /**
     * Determines whether a page View is associated with a specific key object.
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    /**
     * Creates the page for the given position. The adapter is responsible
     * for adding the view to the container given here.
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.layout_fullscreen, container, false);

        final TouchImageView imageView = (TouchImageView) itemView.findViewById(R.id.imageView);
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
                new OperationFailedDialog().show(((PeekFullScreenActivity) mContext).getFragmentManager(), LOG_TAG);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                progressBar.setVisibility(View.VISIBLE);
            }
        };
        imageView.setTag(target);

        // Set up on click listeners
        imageView.setOnClickListener(new OnPictureClickListener());

        Picasso.with(mContext)
                .load(mPictureList.get(position).getFullUrl())
                .resize(1024, 0)
                .onlyScaleDown()
                .into(target);

        container.addView(itemView);

        return itemView;
    }

    /**
     * Removes a page for the given position. The adapter is responsible
     * for removing the view from its container.
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);

    }

    class OnPictureClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ((PeekFullScreenActivity) mContext).finish();
        }
    }

}