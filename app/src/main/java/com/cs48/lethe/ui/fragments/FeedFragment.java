package com.cs48.lethe.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.cs48.lethe.R;
import com.cs48.lethe.ui.activities.FeedFullPictureActivity;
import com.cs48.lethe.ui.adapters.FeedGridAdapter;
import com.cs48.lethe.ui.view_helpers.FeedPullToRefreshLayout;
import com.cs48.lethe.ui.view_helpers.PullToRefreshGridView;
import com.cs48.lethe.utils.ActionCodes;
import com.cs48.lethe.utils.Picture;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FeedFragment extends Fragment {

    public static final String LOG_TAG = FeedFragment.class.getSimpleName();

    private FeedGridAdapter mFeedGridAdapter;

    @InjectView(R.id.feedGridView)
    PullToRefreshGridView mFeedGridView;
    @InjectView(R.id.swipeRefreshLayout)
    FeedPullToRefreshLayout mFeedPullToRefreshLayout;

    /**
     * Sets up the action bar menu.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Creates the feed grid and handles image taps on grid.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);

        ButterKnife.inject(this, rootView);

        mFeedGridAdapter = new FeedGridAdapter(getActivity());
        mFeedGridView.setAdapter(mFeedGridAdapter);
        mFeedGridView.setExpanded(true);

        setGridPullToRefreshGesture();
        setGridTapGesture();

        return rootView;
    }

    /**
     * Starts the full-screen activity and sends the necessary data to
     * that activity through a Bundle.
     */
    private void setGridTapGesture() {
        mFeedGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent feedFullPictureIntent = new Intent(getActivity(), FeedFullPictureActivity.class);

                Picture picture = (Picture) mFeedGridAdapter.getItem(position);
                feedFullPictureIntent.putExtra("uniqueId", picture.getUniqueId());

                startActivityForResult(feedFullPictureIntent, ActionCodes.FEED_FULL_PICTURE_REQUEST);
            }
        });
    }

    /**
     * Sets up the listeners for pull-to-refresh on the grid
     */
    private void setGridPullToRefreshGesture() {

        mFeedGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE && view.getChildAt(0).getTop() >= 0)
                    mFeedPullToRefreshLayout.setEnabled(true);
                else
                    mFeedPullToRefreshLayout.setEnabled(false);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount == 0)
                    mFeedPullToRefreshLayout.setEnabled(true);
            }

        });

        mFeedPullToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mFeedGridAdapter.fetchFeedFromServer(mFeedPullToRefreshLayout);
            }
        });

    }

    /**
     * Hides the delete all images and copy image button in the action bar.
     */
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_clear_cache).setVisible(true);
    }

    /**
     * Handles action bar menu button clicks.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /**
         * Clears the images in the cache and refreshes the grid.
         */
        if (id == R.id.action_clear_cache) {
            mFeedGridAdapter.clearCache();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Hides the image from the feed or updates the database with the new
     * likes and views when returning from the full screen activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ActionCodes.FEED_FULL_PICTURE_REQUEST && resultCode == ActionCodes.HIDE_PICTURE)
            fetchFeedFromServer();
    }

    /**
     * Gets the list of images from the server.
     */
    public void fetchFeedFromServer() {
        mFeedGridAdapter.fetchFeedFromServer(null);
    }
}
