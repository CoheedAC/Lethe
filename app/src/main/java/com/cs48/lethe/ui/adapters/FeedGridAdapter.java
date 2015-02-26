package com.cs48.lethe.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.networking.HerokuRestClient;
import com.cs48.lethe.ui.activities.MainActivity;
import com.cs48.lethe.ui.dialogs.NetworkUnavailableDialog;
import com.cs48.lethe.ui.dialogs.OperationFailedDialog;
import com.cs48.lethe.ui.view_helpers.FeedPullToRefreshLayout;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.NetworkUtilities;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A BaseAdapter that handles the grid for the feed tab.
 */
public class FeedGridAdapter extends BaseAdapter {

    public static final String LOG_TAG = FeedGridAdapter.class.getSimpleName();

    private Context mContext;
    private List<Picture> mPictureList;
    private DatabaseHelper mDatabaseHelper;

    public FeedGridAdapter(Context context) {
        mContext = context;
        mDatabaseHelper = DatabaseHelper.getInstance(mContext);
        fetchFeedFromDatabase();
        fetchFeedFromServer(null);
    }

    /**
     * Returns the number of items in the grid.
     */
    public int getCount() {
        return mPictureList.size();
    }

    /**
     * Returns the File in the ImageList of Files at the
     * given index.
     */
    public Object getItem(int position) {
        return mPictureList.get(position);
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
        ImageView imageView = (ImageView) convertView;
        if (imageView == null) {
            imageView = new ImageView(mContext);
            GridView gridView = (GridView) parent;
            DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

            int imageDimension = metrics.widthPixels / gridView.getNumColumns();
            imageView.setLayoutParams(new GridView.LayoutParams(imageDimension, imageDimension));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundColor(mContext.getResources().getColor(R.color.empty_image));
        }
        Picasso.with(mContext)
                .load(mPictureList.get(position).getThumbnailUrl())
                .resize(200, 0)
                .onlyScaleDown()
                .into(imageView);
        return imageView;
    }

    /**
     * Gets the list of images from the server and adds
     * them to the internal database. Then updates the
     * grid with the new list of images from the
     * internal database.
     */
    public void fetchFeedFromServer(final FeedPullToRefreshLayout feedPullToRefreshLayout) {
        // check if there is internet
        if (NetworkUtilities.isNetworkAvailable(mContext)) {
            if (feedPullToRefreshLayout != null)
                feedPullToRefreshLayout.setRefreshing(true);

            // get current location
            String[] coordinates = NetworkUtilities.getCurrentLocation(mContext);
            String url = mContext.getString(R.string.server_recent) +
                    coordinates[1].replace(".", "a") + "," +    // latitude
                    coordinates[0].replace(".", "a");           // longitude

            HerokuRestClient.get(url, null, new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        // temporary list to store list of images
                        List<Picture> tmpPictureList = new ArrayList<>();

                        // parses the data received from the server
                        String jsonData = new String(responseBody);
                        JSONArray jsonArray = new JSONArray(jsonData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            // adds a new image to the list with the info from the server
                            tmpPictureList.add(new Picture(
                                    jsonObject.getString(mContext.getString(R.string.json_id)),
                                    new SimpleDateFormat("yyyyMMdd_HHmmssSS").format(new Date()),
//                                jsonObject.getString(mContext.getString(R.string.json_date_posted)),
                                    jsonObject.getString(mContext.getString(R.string.json_url_thumbnail)),
                                    jsonObject.getString(mContext.getString(R.string.json_url_full)),
                                    jsonObject.getInt(mContext.getString(R.string.json_views)),
                                    jsonObject.getInt(mContext.getString(R.string.json_likes))));
                        }

                        // updates the database with the new image list
                        // (while keeping the integrity of mImageList)
                        mDatabaseHelper.updateFeed(tmpPictureList);



                        // gets an updated list of images from the database
                        mPictureList = mDatabaseHelper.getFeedPictures();

                        for (int i = 0; i < mPictureList.size(); i++)
                            Log.d(LOG_TAG, mPictureList.get(i).getUniqueId() + " : " + i);

                        if (feedPullToRefreshLayout != null)
                            feedPullToRefreshLayout.setRefreshing(false);

                        // updates the grid to reflect the new data in the image list
                        notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    new OperationFailedDialog().show(((MainActivity) mContext).getFragmentManager(), LOG_TAG);
                    if (feedPullToRefreshLayout != null)
                        feedPullToRefreshLayout.setRefreshing(false);
                    FileUtilities.logResults(mContext, LOG_TAG, "Failed to get feed");
                }
            });
        } else {
            new NetworkUnavailableDialog().show(((Activity) mContext).getFragmentManager(), LOG_TAG);
        }
    }

    /**
     * Hides the image from the feed by removing it from the
     * list of images. The VISIBLE flag has already been
     * set to HIDE_PICTURE in the database in the swipeRight()
     * in the FullPictureActivity class
     */
    public void fetchFeedFromDatabase() {
        mPictureList = mDatabaseHelper.getFeedPictures();
        notifyDataSetChanged();
    }

    /**
     * Clears the database of posted images and clears
     * the list of images
     */
    public void clearCache() {
        mDatabaseHelper.clearFeedTable();
        mPictureList = new ArrayList<>();
        notifyDataSetChanged();
    }

}