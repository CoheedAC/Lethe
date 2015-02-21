package com.cs48.lethe.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.cs48.lethe.R;
import com.cs48.lethe.utils.Image;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * A BaseAdapter that handles the grid for the feed tab.
 */
public class FeedGridViewAdapter extends BaseAdapter {

    public static final String TAG = FeedGridViewAdapter.class.getSimpleName();

    private Context mContext;
    private List<Image> mImageList;

    public FeedGridViewAdapter(Context context, List<Image> imageList) {
        mContext = context;
        mImageList = imageList;
    }

    public void setImageList(List<Image> list) {
        mImageList = list;
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
        Picasso.with(mContext).load(image.getUrl())
                .into(imageView);

//        Uri imageUri = Uri.fromFile(mImageList.get(position));
//        imageView.setImageBitmap(FileUtilities.getThumbnailSizedBitmap(mContext.getContentResolver(), imageUri));

        return imageView;
    }

    /**
     * Creates a new ImageList object with the updated images
     * in the storage directory and then refreshes the grid to reflect
     * the new image(s).
     */
    public void update() {
//        mImageList = FileUtilities.getCachedImagez(mContext);
//        mImages = new Image[mImageList.size()];
//        for (int i = 0; i < mImages.length; i++)
//            mImages[i] = new Image(mImageList.get(i));
        notifyDataSetChanged();
    }

    /**
     * Deletes all of the images downloaded from the server that
     * are stored in the cache folder.
     */
    public void clearCache() {
        File cachedDirectory = new File(mContext.getCacheDir() + File.separator + "picasso-cache");
        for (File cachedFile : cachedDirectory.listFiles())
            Picasso.with(mContext).invalidate(cachedFile);

        update();
    }

}