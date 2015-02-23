package com.cs48.lethe.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.cs48.lethe.R;
import com.cs48.lethe.server.RequestFeed;
import com.cs48.lethe.ui.dialogs.NetworkUnavailableDialog;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.Image;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A BaseAdapter that handles the grid for the feed tab.
 */
public class FeedGridViewAdapter extends BaseAdapter {

    public static final String TAG = FeedGridViewAdapter.class.getSimpleName();

    private Context mContext;
    private List<Image> mImageList;

    public FeedGridViewAdapter(Context context) {
        mContext = context;
        mImageList = new ArrayList<>();
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
        Picasso.with(mContext)
                .load(image.getThumbnailUrl())
                .into(imageView);

        return imageView;
    }

    public void requestFeed() {
        if (FileUtilities.isNetworkAvailable(mContext)) {
            String[] coordinates = FileUtilities.getLocationCoordinates(mContext);
            new RequestFeed(mContext, this).execute(coordinates);
        } else {
            new NetworkUnavailableDialog().show(((Activity) mContext).getFragmentManager(), TAG);
        }
    }

    /**
     * Deletes all of the images downloaded from the server that
     * are stored in the cache folder.
     */
    public void clearCache() {
//        File cachedDirectory = new File(mContext.getCacheDir() + File.separator + "picasso-cache");
//        for (File cachedFile : cachedDirectory.listFiles()) {
//            Picasso.with(mContext).invalidate(cachedFile);
//            cachedFile.delete();
//        }
//        mImageList.removeAll(mImageList);
//
//        notifyDataSetChanged();
    }

}