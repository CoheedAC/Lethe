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
 * Created by maxkohne on 1/29/15.
 */
public class MeGridViewAdapter extends BaseAdapter {

    public static final String TAG = MeGridViewAdapter.class.getSimpleName();

    private List<File> mImageList;
    private Context mContext;

    public MeGridViewAdapter(Context context) {
        mContext = context;
        mImageList = FileUtilities.getPostedImages(context);
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
            imageView.setBackgroundColor(mContext.getResources().getColor(R.color.image_load));
        }

        Uri imageUri = Uri.fromFile(mImageList.get(position));
        imageView.setImageURI(imageUri);
        return imageView;
    }

    public void update() {
        mImageList = FileUtilities.getPostedImages(mContext);
        notifyDataSetChanged();
    }

    public void deleteAllImages() {
        FileUtilities.deletePostedImages(mContext);
        update();
        Toast.makeText(mContext, "Deleted all images", Toast.LENGTH_LONG).show();
    }

    public void copyImage() {
        try {
            FileUtilities.copyFile(mContext, mImageList.get(0).getAbsolutePath(), 50);
            update();
            Toast.makeText(mContext, "Copied first image", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(mContext, "No image to copy", Toast.LENGTH_LONG).show();
        }
    }

}