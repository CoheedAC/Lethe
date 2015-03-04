package com.cs48.lethe.ui.peek;

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
import com.cs48.lethe.ui.alertdialogs.OperationFailedAlertDialog;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.PictureUtilities;
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
 * Created by maxkohne on 2/26/15.
 */
public class PeekGridViewAdapter extends BaseAdapter {

    public static final String LOG_TAG = PeekGridViewAdapter.class.getSimpleName();

    private Context mContext;
    private List<Picture> mPictureList;
    private DatabaseHelper mDatabaseHelper;

    public PeekGridViewAdapter(Context context) {
        mContext = context;
        mDatabaseHelper = DatabaseHelper.getInstance(mContext);
        clearPeekFeed();
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
     *
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
     *
     * @return The id of the item at the specified position
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     *
     * @param position The position of the item within the adapter's
     *                 data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to.
     *
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
        Picasso.with(mContext)
                .load(mPictureList.get(position).getThumbnailUrl())
                .resize(PictureUtilities.MAX_THUMBNAIL_WIDTH, 0)
                .onlyScaleDown()
                .into(imageView);
        return imageView;
    }

    /**
     * Clears the database of posted images and clears
     * the list of images
     */
    public void clearPeekFeed() {
        mPictureList = new ArrayList<>();
        mDatabaseHelper.clearPeekTable();
        notifyDataSetChanged();
    }

    /**
     * Gets the list of images from the server and adds
     * them to the internal database. Then updates the
     * grid with the new list of images from the
     * internal database. This also tells the pull-to-refresh
     * to start the refresh animation before a server response
     * and to stop on a server response.
     */
    public void fetchPeekFeedFromServer(final PeekFragment peekFragment, String latitude, String longitude) {
        clearPeekFeed();
        peekFragment.setEmptyGridMessage("");
        // url with specified latitude and longitude
        String url = mContext.getString(R.string.server_recent) +
                longitude.replace(".", "a") + "," +    // latitude
                latitude.replace(".", "a");           // longitude

        HerokuRestClient.get(url, null, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                peekFragment.stopRefreshingAnimation();
                try {
                    // parses the data received from the server
                    String jsonData = new String(responseBody);
                    JSONArray jsonArray = new JSONArray(jsonData);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        // adds a new image to the list with the info from the server
                        mPictureList.add(new Picture(
                                jsonObject.getString(mContext.getString(R.string.json_id)),
                                new SimpleDateFormat("yyyyMMdd_HHmmssSS").format(new Date()),
//                                jsonObject.getString(mContext.getString(R.string.json_date_posted)),
                                jsonObject.getString(mContext.getString(R.string.json_url_thumbnail)),
                                jsonObject.getString(mContext.getString(R.string.json_url_full)),
                                jsonObject.getInt(mContext.getString(R.string.json_views)),
                                jsonObject.getInt(mContext.getString(R.string.json_likes))));
                    }
                    peekFragment.setEmptyGridMessage(mContext.getString(R.string.grid_area_empty));

                    mDatabaseHelper.updatePeekFeed(mPictureList);

                    // updates the grid to reflect the new data in the image list
                    notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                peekFragment.stopRefreshingAnimation();
                if (!peekFragment.setEmptyGridMessage(mContext.getString(R.string.grid_error))) {
                    try {
                        new OperationFailedAlertDialog().show(peekFragment.getActivity().getFragmentManager(), LOG_TAG);
                    } catch (IllegalStateException e) {
                        Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                    }
                }
            }
        });
    }
}