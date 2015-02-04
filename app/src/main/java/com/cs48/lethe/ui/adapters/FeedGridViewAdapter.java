package com.cs48.lethe.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.cs48.lethe.utils.FileUtilities;

import java.io.File;
import java.util.List;

/**
 * Created by maxkohne on 1/29/15.
 */
public class FeedGridViewAdapter extends BaseAdapter {

    private List<File> mImageList;
    private Context mContext;

    public FeedGridViewAdapter(Context context) {
        mContext = context;
        mImageList = FileUtilities.listFiles(mContext);
    }

    public int getCount() {
        return mImageList.size();
    }

    public Object getItem(int position) {
        return mImageList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = (ImageView) convertView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            GridView.LayoutParams imageParams = new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    300);
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            int padding = 5;
            imageView.setPadding(padding, padding, padding, padding);
        }

        Uri imageUri = Uri.fromFile(mImageList.get(position));
        imageView.setImageURI(imageUri);

        return imageView;
    }

    public void update() {
        mImageList = FileUtilities.listFiles(mContext);
        notifyDataSetChanged();
    }

}