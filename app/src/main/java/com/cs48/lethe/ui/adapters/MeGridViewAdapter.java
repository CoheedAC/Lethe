package com.cs48.lethe.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.utils.FileUtilities;

import java.io.File;
import java.util.List;

/**
 * A BaseAdapter that handles the grid for the me tab.
 */
public class MeGridViewAdapter extends BaseAdapter {

    public static final String TAG = MeGridViewAdapter.class.getSimpleName();

    private List<File> mImageList;
    private Context mContext;

    public MeGridViewAdapter(Context context) {
        mContext = context;
        mImageList = FileUtilities.getPostedImages(context);
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

        Uri imageUri = Uri.fromFile(mImageList.get(position));
        //imageView.setImageURI(imageUri);
        imageView.setImageBitmap(FileUtilities.getThumbnailSizedBitmap(mContext.getContentResolver(), imageUri)  );
        return imageView;
    }

    /**
     * Creates a new ImageList object with the updated images
     * in the storage directory and then refreshes the grid to reflect
     * the new image(s).
     */
    public void update() {
        mImageList = FileUtilities.getPostedImages(mContext);
        notifyDataSetChanged();
    }

    /**
     * Deletes all of the images taken from this app.
     */
    public void deleteAllImages() {
        FileUtilities.deletePostedImages(mContext);
        update();
        Toast.makeText(mContext, "Deleted all images", Toast.LENGTH_LONG).show();
    }

    /**
     * Copies the first image in the grid 50 times to test a full grid.
     */
    public void copyImage() {
        try {
            FileUtilities.copyImage(mContext, mImageList.get(0).getAbsolutePath(), 50);
            update();
            Toast.makeText(mContext, "Copied first image", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(mContext, "No image to copy", Toast.LENGTH_LONG).show();
        }
    }

}