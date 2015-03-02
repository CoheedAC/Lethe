package com.cs48.lethe.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.cs48.lethe.R;
import com.cs48.lethe.ui.activities.FeedFullScreenActivity;
import com.cs48.lethe.ui.adapters.FeedGridViewAdapter;
import com.cs48.lethe.ui.dialogs.NetworkUnavailableDialog;
import com.cs48.lethe.ui.view_helpers.FeedPullToRefreshLayout;
import com.cs48.lethe.ui.view_helpers.PullToRefreshGridView;
import com.cs48.lethe.utils.NetworkUtilities;
import com.cs48.lethe.utils.Picture;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FeedFragment extends Fragment {

    public static final String LOG_TAG = FeedFragment.class.getSimpleName();

    private FeedGridViewAdapter mFeedGridViewAdapter;

    @InjectView(R.id.feedGridView)
    PullToRefreshGridView mFeedGridView;
    @InjectView(R.id.swipeRefreshLayout)
    FeedPullToRefreshLayout mFeedPullToRefreshLayout;
    @InjectView(R.id.emptyGridTextView)
    TextView mEmptyGridTextView;

    /**
     * Called to do initial creation of a fragment. This is called after onAttach(Activity)
     * and before onCreateView(LayoutInflater, ViewGroup, Bundle). Note that this can be called
     * while the fragment's activity is still in the process of being created. As such, you can
     * not rely on things like the activity's content view hierarchy being initialized at this point.
     *
     * @param savedInstanceState If the fragment is being re-created from a
     *                           previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mFeedGridViewAdapter = new FeedGridViewAdapter(getActivity());
    }

    /**
     * Called to have the fragment instantiate its user interface view. This
     * will be called between onCreate(Bundle) and onActivityCreated(Bundle).
     * If you return a View from here, you will later be called in
     * onDestroyView() when the view is being released.
     *
     * @param inflater The LayoutInflater object that can be used
     *                 to inflate any views in the fragment,
     * @param container If non-null, this is the parent view that the
     *                  fragment's UI should be attached to. The fragment
     *                  should not add the view itself, but this can be
     *                  used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);

        ButterKnife.inject(this, rootView);

        mFeedGridView.setAdapter(mFeedGridViewAdapter);
        mFeedGridView.setExpanded(true);

        if (!NetworkUtilities.isNetworkAvailable(getActivity())) {
            setEmptyGridMessage(getString(R.string.grid_no_internet_connection));
        } else {
            setEmptyGridMessage(getString(R.string.grid_area_empty));
            fetchFeedFromServer();
        }

        mFeedGridView.setOnItemClickListener(new OnPictureClickListener());
        mFeedGridView.setOnScrollListener(new OnScrollListener());
        mFeedPullToRefreshLayout.setOnRefreshListener(new OnRefreshListener());

        return rootView;
    }

    /**
     * Called when the fragment is visible to the user and actively running. This is
     * generally tied to Activity.onResume of the containing Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (NetworkUtilities.isNetworkAvailable(getActivity()))
            fetchFeedFromServer();
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     * You should place your menu items in to menu. For this method to be
     * called, you must have first called setHasOptionsMenu(boolean).
     *
     * @param menu The options menu in which you place your items.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment,
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_clear_cache).setVisible(true);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item The menu item that was selected.
     *
     * @return Return false to allow normal menu processing to proceed,
     *         true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /**
         * Clears the images in the cache and refreshes the grid.
         */
        if (id == R.id.action_clear_cache) {
            mFeedGridViewAdapter.clearCache();
            setEmptyGridMessage(getString(R.string.grid_area_empty));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean setEmptyGridMessage(String errorMessage) {
        if (mFeedGridViewAdapter.getCount() == 0) {
            mEmptyGridTextView.setVisibility(View.VISIBLE);
            mEmptyGridTextView.setText(errorMessage);
            return true;
        }
        mEmptyGridTextView.setVisibility(View.GONE);
        return false;
    }

    /**
     * Gets the list of images from the server.
     */
    public void fetchFeedFromServer() {
        if (NetworkUtilities.isNetworkAvailable(getActivity())) {
            mFeedGridViewAdapter.fetchFeedFromServer(this);
        } else {
            mFeedPullToRefreshLayout.setRefreshing(false);
            if (!setEmptyGridMessage(getString(R.string.grid_no_internet_connection))) {
                try {
                    new NetworkUnavailableDialog().show(getActivity().getFragmentManager(), LOG_TAG);
                } catch (IllegalStateException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }
        }
    }

    public void stopRefreshAnimation() {
        mFeedPullToRefreshLayout.setRefreshing(false);
    }


    /**
     * A callback to be invoked when an item in this AdapterView has been clicked.
     */
    class OnPictureClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent feedFullPictureIntent = new Intent(getActivity(), FeedFullScreenActivity.class);
            Picture picture = (Picture) mFeedGridViewAdapter.getItem(position);
            feedFullPictureIntent.putExtra(getString(R.string.data_uniqueId), picture.getUniqueId());
            startActivity(feedFullPictureIntent);
        }
    }

    /**
     * Sets up the scroll listeners for pull-to-refresh on the grid
     */
    class OnScrollListener implements AbsListView.OnScrollListener {
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
    }

    /**
     * Sets up the refresh listeners for pull-to-refresh feature
     */
    class OnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            fetchFeedFromServer();
        }
    }
}
