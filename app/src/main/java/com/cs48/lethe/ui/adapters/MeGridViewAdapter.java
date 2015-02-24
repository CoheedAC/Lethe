package com.cs48.lethe.ui.adapters;

import android.content.Context;
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
import com.cs48.lethe.utils.Image;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * A BaseAdapter that handles the grid for the me tab.
 */
public class MeGridViewAdapter extends BaseAdapter {

    public static final String TAG = MeGridViewAdapter.class.getSimpleName();

    private Context mContext;
    private List<Image> mImageList;
    private DatabaseHelper mDatabaseHelper;

    public MeGridViewAdapter(Context context) {
        mContext = context;
        mDatabaseHelper = DatabaseHelper.getInstance(mContext);
        mImageList = mDatabaseHelper.getPostedImages();
    }

    public void fetchImagesFromDatabase() {
        mImageList = mDatabaseHelper.getPostedImages();
        notifyDataSetChanged();
    }

    /**
     * Returns the number of items in the grid.
     */
    public int getCount() {
        return mImageList.size();
    }

    /**
     * Returns the File in the ImageList of Files at the
     * given index.
     */
    public Object getItem(int position) {
        return mImageList.get(position);
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
        ImageView imageView = (ImageView) convertView;

        // if it's not recycled, initialize some attributes
        if (convertView == null) {
            imageView = new ImageView(mContext);
            GridView.LayoutParams imageParams = new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    300);
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundColor(mContext.getResources().getColor(R.color.image_load));
        }

        Image image = (Image) getItem(position);
        Picasso.with(mContext).load(image.getFile()).into(imageView);

        return imageView;
    }

    /**
     * Deletes all of the images taken from this app.
     */
    public void deleteAllPostedImages() {
        mDatabaseHelper.clearPostedImages();
        for(Image image: mImageList)
            image.getFile().delete();
        mImageList.removeAll(mImageList);
        notifyDataSetChanged();
        FileUtilities.logResults(mContext, TAG, "Deleted all images");
    }

    /**
     * Deletes the image from the internal database, deletes
     * the file stored on the device, removes the image
     * from the list of images, and updates the grid
     * to reflect the deletion.
     */
    public void deletePostedImage(int position) {
        if (position >= 0 && position < mImageList.size()) {
            mImageList.get(position).getFile().delete();
            mImageList.remove(position);
            notifyDataSetChanged();
        }
    }

    /**
     * Updates the image likes and dislikes from the server
     */
    public void updateImageStatistics(int position) {
        if (position >= 0 && position < mImageList.size())
            mDatabaseHelper.updateImageStatisticsFromDatabase(mImageList.get(position));
    }

    /**
     * Populates 50 temporary image objects from the
     * first image object in the image list.
     *
     * WARNING: if you press delete in the full screen view
     * it will delete all 50 images
     */
    public void copyFirstImage() {
        try {
            Image image = mImageList.get(0);
            File src = image.getFile();
            Log.d(TAG, src.getAbsolutePath());

            for (int i = 0; i < 50; i++)
                mImageList.add(new Image(image.getUniqueId(),
                        image.getDatePosted(),
                        image.getFile(),
                        image.getViews(),
                        image.getLikes()));
            new DeleteImageWarningDialog().show(((MainActivity) mContext).getFragmentManager(), TAG);
            notifyDataSetChanged();
            FileUtilities.logResults(mContext, TAG, "Copied first image");
        } catch (Exception e) {
            FileUtilities.logResults(mContext, TAG, "No image to copy");
        }
    }

}