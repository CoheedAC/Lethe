package com.cs48.lethe.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.cs48.lethe.R;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.Image;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A BaseAdapter that handles the grid for the me tab.
 */
public class MeGridViewAdapter extends BaseAdapter {

    public static final String TAG = MeGridViewAdapter.class.getSimpleName();

    private List<Image> mImageList;
    private Context mContext;

    public MeGridViewAdapter(Context context) {
        mContext = context;
        List<File> postedImageFiles = FileUtilities.getPostedImages(mContext);
        mImageList = new ArrayList<>();
        for (File imageFile : postedImageFiles) {
            mImageList.add(new Image(imageFile.getAbsolutePath()));
        }
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
                .load(image.getFile())
                .into(imageView);

//        imageView.setImageBitmap(FileUtilities.getThumbnailSizedBitmap(mContext.getContentResolver(), imageUri)  );
        return imageView;
    }

    /**
     * Creates a new ImageList object with the updated images
     * in the storage directory and then refreshes the grid to reflect
     * the new image(s).
     */
    public void update() {
        List<File> postedImageFiles = FileUtilities.getPostedImages(mContext);
        mImageList.removeAll(mImageList);
        for (File imageFile : postedImageFiles) {
            mImageList.add(new Image(imageFile.getAbsolutePath()));
        }
        notifyDataSetChanged();
    }

    /**
     * Deletes all of the images taken from this app.
     */
    public void deletePostedImages() {
        String subdirectory = FileUtilities.getSubdirectoryName(mContext);
        File sharedExternalDirectory = FileUtilities.getSharedExternalDirectory(mContext);
        for (File sharedFile : sharedExternalDirectory.listFiles()) {
            sharedFile.delete();
        }
        File externalFilesDir = mContext.getExternalFilesDir(subdirectory);
        for (File savedFile : externalFilesDir.listFiles()) {
            savedFile.delete();
        }

        update();
        FileUtilities.logResults(mContext, TAG, "Deleted all images");
    }

    /**
     * Copies the first image in the grid 50 times to test a full grid.
     */
    public void copyFirstImage() {
//        try {
//            File dir = FileUtilities.getFileDirectory(mContext);
//            String src = String imageCopyName = FileUtilities.getUniqueId(FileUtilities.getSimpleName(src));
//            for (int i = 0; i < 50; i++) {
//
//                File dst = new File(dir + "/IMG_" + imageCopyName + "_" + i + ".jpg");
//
//                InputStream in = new FileInputStream(src);
//                OutputStream out = new FileOutputStream(dst);
//
//                // Transfer bytes from in to out
//                byte[] buf = new byte[1024];
//                int len;
//                while ((len = in.read(buf)) > 0) {
//                    out.write(buf, 0, len);
//                }
//
//                mImageList.add(dst);
//                notifyDataSetChanged();
//                in.close();
//                out.close();
//            }
//
//            FileUtilities.logResults(mContext, TAG, "Copied first image");
//        } catch (Exception e) {
//            FileUtilities.logResults(mContext, TAG, "No image to copy");
//        }
    }

}