package com.cs48.lethe.ui.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.ui.activities.MainActivity;
import com.cs48.lethe.ui.dialogs.DeleteImageWarningDialog;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.Picture;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * A BaseAdapter that handles the grid for the me tab.
 */
public class MeGridAdapter extends BaseAdapter {

    public static final String LOG_TAG = MeGridAdapter.class.getSimpleName();

    private Context mContext;
    private List<Picture> mPictureList;
    private DatabaseHelper mDatabaseHelper;

    public MeGridAdapter(Context context) {
        mContext = context;
        mDatabaseHelper = DatabaseHelper.getInstance(mContext);
        fetchMePicturesFromDatabase();
    }

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
                .resize(200, 0)
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

    /**
     * Populates 50 temporary image objects from the
     * first image object in the image list.
     * <p/>
     * WARNING: if you press delete in the full screen view
     * it will delete all 50 images
     */
    public void copyFirstImage() {
        try {
            Picture picture = mPictureList.get(0);
            File src = picture.getFile();
            Log.d(LOG_TAG, src.getAbsolutePath());

            for (int i = 0; i < 50; i++)
                mPictureList.add(new Picture(picture.getUniqueId(),
                        picture.getDatePosted(),
                        picture.getFile(),
                        picture.getViews(),
                        picture.getLikes()));
            new DeleteImageWarningDialog().show(((MainActivity) mContext).getFragmentManager(), LOG_TAG);
            notifyDataSetChanged();
        } catch (Exception e) {
            FileUtilities.logResults(mContext, LOG_TAG, "No image to copy");
        }
    }

}