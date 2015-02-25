package com.cs48.lethe.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import com.squareup.picasso.Target;

import java.io.File;
import java.util.List;

/**
 * A BaseAdapter that handles the grid for the me tab.
 */
public class MeGridViewAdapter extends BaseAdapter {

    public static final String LOG_TAG = MeGridViewAdapter.class.getSimpleName();

    private Context mContext;
    private List<Image> mImageList;
    private DatabaseHelper mDatabaseHelper;
    private Target mTarget;

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
        // if it's not recycled, initialize some attributes
        if (convertView == null) {
            final ImageView imageView = new ImageView(mContext);
            GridView.LayoutParams imageParams = new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    300);
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundColor(mContext.getResources().getColor(R.color.image_load));

            mTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    imageView.setImageBitmap(FileUtilities.getThumbnailSizedBitmap(bitmap));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };
            imageView.setTag(mTarget);

            Picasso.with(mContext).load(mImageList.get(position).getFile()).into(mTarget);
            return imageView;
        } else {
            final ImageView imageView = (ImageView) convertView;

            mTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    imageView.setImageBitmap(FileUtilities.getThumbnailSizedBitmap(bitmap));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };
            imageView.setTag(mTarget);

            Picasso.with(mContext).load(mImageList.get(position).getFile()).into(mTarget);
            return imageView;
        }
    }

    /**
     * Deletes all of the images taken from this app.
     */
    public void deleteAllPostedImages() {
        mDatabaseHelper.clearPostedImages();
        for (Image image : mImageList)
            image.getFile().delete();
        mImageList.removeAll(mImageList);
        notifyDataSetChanged();
        FileUtilities.logResults(mContext, LOG_TAG, "Deleted all images");
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
     * <p/>
     * WARNING: if you press delete in the full screen view
     * it will delete all 50 images
     */
    public void copyFirstImage() {
        try {
            Image image = mImageList.get(0);
            File src = image.getFile();
            Log.d(LOG_TAG, src.getAbsolutePath());

            for (int i = 0; i < 50; i++)
                mImageList.add(new Image(image.getUniqueId(),
                        image.getDatePosted(),
                        image.getFile(),
                        image.getViews(),
                        image.getLikes()));
            new DeleteImageWarningDialog().show(((MainActivity) mContext).getFragmentManager(), LOG_TAG);
            notifyDataSetChanged();
            FileUtilities.logResults(mContext, LOG_TAG, "Copied first image");
        } catch (Exception e) {
            FileUtilities.logResults(mContext, LOG_TAG, "No image to copy");
        }
    }

}