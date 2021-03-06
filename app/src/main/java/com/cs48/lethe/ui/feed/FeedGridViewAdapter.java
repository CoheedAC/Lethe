package com.cs48.lethe.ui.feed;

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
import com.cs48.lethe.ui.alertdialogs.OperationFailedAlertDialog;
import com.cs48.lethe.utils.HerokuRestClient;
import com.cs48.lethe.utils.NetworkUtilities;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.PictureUtilities;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A BaseAdapter that handles the grid for the feed tab.
 */
public class FeedGridViewAdapter extends BaseAdapter {

    // Logcat tag
    public static final String LOG_TAG = FeedGridViewAdapter.class.getSimpleName();

    // Instance variables
    private Context mContext;
    private List<Picture> mPictureList;
    private DatabaseHelper mDatabaseHelper;

    /**
     * Constructor that retrieves pictures from the database
     *
     * @param context Interface to global information about an application environment
     */
    public FeedGridViewAdapter(Context context) {
        mContext = context;
        mDatabaseHelper = DatabaseHelper.getInstance(mContext);
        if (NetworkUtilities.isNetworkAvailable(mContext)) {
            fetchFeedFromDatabase();
        }else
            mPictureList = new ArrayList<>();
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return mPictureList.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's data set.
     * @return The data at the specified position.
     */
    @Override
    public Picture getItem(int position) {
        return mPictureList.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     *
     * @param position    The position of the item within the adapter's
     *                    data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @Override
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
        // Load the picture into the imageview
        if (mPictureList.get(position).getFile() == null) {
            Picasso.with(mContext)
                    .load(mPictureList.get(position).getThumbnailUrl())
                    .resize(PictureUtilities.MAX_THUMBNAIL_WIDTH, 0)
                    .onlyScaleDown()
                    .rotate(mPictureList.get(position).getOrientation())
                    .into(imageView);
        } else {
            Picasso.with(mContext)
                    .load(mPictureList.get(position).getFile())
                    .resize(PictureUtilities.MAX_THUMBNAIL_WIDTH, 0)
                    .onlyScaleDown()
                    .rotate(mPictureList.get(position).getOrientation())
                    .into(imageView);
        }
        return imageView;
    }

    /**
     * Gets the list of images from the server and adds
     * them to the internal database. Then updates the
     * grid with the new list of images from the
     * internal database.
     *
     * @param feedFragment A reference to the Feed Fragment
     * @param latitude Latitude of the current location
     * @param longitude Longitude of the current location
     */
    public void fetchFeedFromServer(final FeedFragment feedFragment, final double latitude, final double longitude) {
        feedFragment.setEmptyGridMessage("");
        String url = mContext.getString(R.string.server_recent) +
                String.valueOf(latitude).replace(".", "a") + "," +
                String.valueOf(longitude).replace(".", "a");

        HerokuRestClient.get(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    // temporary list to store list of images
                    Map<String, Picture> serverPictureMap = new HashMap<>();

                    // parses the data received from the server
                    String jsonData = new String(responseBody);
                    JSONArray jsonArray = new JSONArray(jsonData);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        Picture picture = new Picture(
                                jsonObject.getString(mContext.getString(R.string.json_id)),
                                latitude,
                                longitude,
                                null,
                                jsonObject.getString(mContext.getString(R.string.json_url_thumbnail)),
                                jsonObject.getString(mContext.getString(R.string.json_url_full)),
                                jsonObject.getInt(mContext.getString(R.string.json_orientation)),
                                jsonObject.getInt(mContext.getString(R.string.json_views)),
                                jsonObject.getInt(mContext.getString(R.string.json_likes)),
                                jsonObject.getString(mContext.getString(R.string.json_date_posted)));

                        serverPictureMap.put(picture.getUniqueId(), picture);
                    }

                    // Deletes pictures in the database that
                    // are not in the list of pictures retrieved
                    // from the server
                    for (Picture feedPicture : mPictureList) {
                        Picture serverPicture = serverPictureMap.get(feedPicture.getUniqueId());

                        // if the feed picture was not found in the server list, then delete it
                        if (serverPicture == null)
                            mDatabaseHelper.deletePictureFromFeedTable(feedPicture);
                    }

                    // updates the database with the new image list
                    // (while keeping the integrity of mImageList)
                    mDatabaseHelper.updateFeed(serverPictureMap);

                    // gets an updated list of pictures from the database
                    fetchFeedFromDatabase();
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
                feedFragment.stopRefreshAnimation();
                feedFragment.setEmptyGridMessage(mContext.getString(R.string.grid_area_empty));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                feedFragment.stopRefreshAnimation();
                if (!feedFragment.setEmptyGridMessage(feedFragment.getString(R.string.grid_error))) {
                    try {
                        new OperationFailedAlertDialog().show(feedFragment.getActivity().getFragmentManager(), LOG_TAG);
                    } catch (IllegalStateException e) {
                        Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                    }
                }
            }
        });
    }

    /**
     * Gets the pictures from the Feed Table in
     * the database
     */
    public void fetchFeedFromDatabase() {
        mPictureList = mDatabaseHelper.getFeedPictures();
        notifyDataSetChanged();
    }

}