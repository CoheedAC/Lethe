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

/**
 * Created by maxkohne on 1/29/15.
 */
public class MeGridViewAdapter extends BaseAdapter {

    public final String TAG = MeGridViewAdapter.class.getSimpleName();

    private File[] images;
    private Context mContext;

    public MeGridViewAdapter(Context context) {
        mContext = context;
        images = FileUtilities.listFiles(context);
    }

    public int getCount() {
        return images.length;
    }

    public Object getItem(int position) {
        return images[position];
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
            int pad = 10;
            imageView.setPadding(pad, pad, pad, pad);
        }

        Uri imageUri = FileUtilities.getImageUri(images[position]);
        imageView.setImageURI(imageUri);
        return imageView;
    }

}