package com.cs48.lethe.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.networking.HerokuRestClient;
import com.cs48.lethe.ui.activities.MeFullScreenActivity;
import com.cs48.lethe.ui.dialogs.OperationFailedDialog;
import com.cs48.lethe.ui.view_helpers.TouchImageView;
import com.cs48.lethe.utils.ActionCodes;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.PictureUtilities;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by maxkohne on 2/26/15.
 */
public class MePagerAdapter extends PagerAdapter {

    public final static String LOG_TAG = MePagerAdapter.class.getSimpleName();

    private Context mContext;
    private List<Picture> mPictureList;
    private LayoutInflater mLayoutInflater;
    private DatabaseHelper mDatabaseHelper;

    public MePagerAdapter(Context context) {
        mContext = context;
        mDatabaseHelper = DatabaseHelper.getInstance(mContext);
        mLayoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPictureList = mDatabaseHelper.getMePictures();
    }

    /**
     * Returns the number of items in the fullscreen slideshow
     */
    @Override
    public int getCount() {
        return this.mPictureList.size();
    }

    /**
     * Determines whether a page View is associated with a specific key object.
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    /**
     * Creates the page for the given position. The adapter is responsible
     * for adding the view to the container given here.
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.layout_fullscreen, container, false);

        final TouchImageView imageView = (TouchImageView) itemView.findViewById(R.id.imageView);
        final ProgressBar progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        TextView likesTextView = (TextView) itemView.findViewById(R.id.likesTextView);
        TextView viewsTextView = (TextView) itemView.findViewById(R.id.viewsTextView);
        ImageButton saveButton = (ImageButton) itemView.findViewById(R.id.saveButton);
        ImageButton deleteButton = (ImageButton) itemView.findViewById(R.id.deleteButton);

        likesTextView.setText("Likes: " + mPictureList.get(position).getLikes());
        viewsTextView.setText("Views: " + mPictureList.get(position).getViews());

        // Set up on click listeners
        imageView.setOnClickListener(new OnPictureClickListener());
        deleteButton.setOnClickListener(new OnDeleteButtonClickListener(position));
        saveButton.setOnClickListener(new OnSaveButtonClickListener(position));

        // Display full image
        final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                progressBar.setVisibility(View.GONE);
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                progressBar.setVisibility(View.GONE);
                try {
                    new OperationFailedDialog().show(((MeFullScreenActivity) mContext).getFragmentManager(), LOG_TAG);
                } catch (IllegalStateException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                progressBar.setVisibility(View.VISIBLE);
            }
        };
        imageView.setTag(target);

        Picasso.with(mContext)
                .load(mPictureList.get(position).getFile())
                .resize(PictureUtilities.MAX_FULL_WIDTH, 0)
                .onlyScaleDown()
                .into(target);

        fetchPictureStatisticsFromServer(position, likesTextView, viewsTextView);

        container.addView(itemView);

        return itemView;
    }

    /**
     * Removes a page for the given position. The adapter is responsible
     * for removing the view from its container.
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

    /**
     * Gets the image statistics from the server and updates
     * the internal database with the new statistics. Also
     * displays the new statistics on the screen.
     */
    public void fetchPictureStatisticsFromServer(final int position, final TextView likesTextView, final TextView viewsTextView) {
        HerokuRestClient.get(mPictureList.get(position).getUniqueId(), null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String jsonData = new String(responseBody);
                    JSONObject jsonObject = new JSONObject(jsonData);
                    mPictureList.get(position).setViews(jsonObject.getInt("view"));
//                    mImageList.get(position).setViews(jsonObject.getInt(mContext.getString(R.string.json_views)));
                    mPictureList.get(position).setLikes(jsonObject.getInt(mContext.getString(R.string.json_likes)));

                    mDatabaseHelper.updateDatabaseFromPicture(mPictureList.get(position));

                    likesTextView.setText("Likes: " + mPictureList.get(position).getLikes());
                    viewsTextView.setText("Views: " + mPictureList.get(position).getViews());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(LOG_TAG, "Request for statistics failed");
            }
        });
    }

    class OnPictureClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ((MeFullScreenActivity) mContext).finish();
        }
    }

    class OnDeleteButtonClickListener implements View.OnClickListener {

        int position;

        public OnDeleteButtonClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            MeFullScreenActivity meFullScreenActivity = (MeFullScreenActivity) mContext;
            meFullScreenActivity.setResult(ActionCodes.DELETE_PICTURE, meFullScreenActivity.getIntent());

            // TODO: delete from server. if successful, then delete from internal database
            mDatabaseHelper.deletePictureFromDatabase(mPictureList.get(position));
            Toast.makeText(mContext, "Deleted image", Toast.LENGTH_SHORT).show();
            meFullScreenActivity.finish();
        }
    }

    class OnSaveButtonClickListener implements View.OnClickListener {

        int position;

        public OnSaveButtonClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            try {
                if (FileUtilities.saveImageForSharing(mContext, mPictureList.get(position).getFile().getAbsolutePath()))
                    Toast.makeText(mContext, "Saved picture to shared storage.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext, "Picture already exists in shared storage", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(mContext, "Cannot copy to shared storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}