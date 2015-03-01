package com.cs48.lethe.ui.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.PictureUtilities;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * A BaseAdapter that handles the grid for the me tab.
 */
public class MeGridViewAdapter extends BaseAdapter {

    public static final String LOG_TAG = MeGridViewAdapter.class.getSimpleName();

    private Context mContext;
    private List<Picture> mPictureList;
    private DatabaseHelper mDatabaseHelper;

    public MeGridViewAdapter(Context context) {
        mContext = context;
        mDatabaseHelper = DatabaseHelper.getInstance(mContext);
        fetchMePicturesFromDatabase();
    }

    /**
     * Gets the list of posted pictures from the database.
     */
    public void fetchMePicturesFromDatabase() {
        mPictureList = mDatabaseHelper.getMePictures();
        notifyDataSetChanged();
    }

    /**
     * Returns the number of items in the grid.
     */
    public int getCount() {
        return mPictureList.size();
    }

    /**
     * Returns the File in the ImageList of Files at the
     * given index.
     */
    public Object getItem(int position) {
        return mPictureList.get(position);
    }

    /**
     * Returns the position.
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * Creates a new ImageView for each item referenced by the Adapter
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        // if it's not recycled, initialize some attributes
        // if it's not recycled, initialize some attributes
        ImageView imageView = (ImageView) convertView;
        if (imageView == null) {
            imageView = new ImageView(mContext);
            GridView gridView = (GridView) parent;
            DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

            int imageDimension = metrics.widthPixels / gridView.getNumColumns();
            imageView.setLayoutParams(new GridView.LayoutParams(imageDimension, imageDimension));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundColor(mContext.getResources().getColor(R.color.empty_image));
        }
        Picasso.with(mContext)
                .load(mPictureList.get(position).getFile())
                .resize(PictureUtilities.MAX_THUMBNAIL_WIDTH, 0)
                .onlyScaleDown()
                .into(imageView);
        return imageView;
    }

    /**
     * Deletes all of the images taken from this app.
     */
    public void deleteAllPostedImages() {
        mDatabaseHelper.clearMeTable();
        for (Picture picture : mPictureList)
            picture.getFile().delete();
        mPictureList.removeAll(mPictureList);
        notifyDataSetChanged();
    }

}