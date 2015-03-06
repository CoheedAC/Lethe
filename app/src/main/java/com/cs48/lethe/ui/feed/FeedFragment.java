package com.cs48.lethe.ui.feed;

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
import com.cs48.lethe.ui.alertdialogs.NetworkUnavailableAlertDialog;
import com.cs48.lethe.ui.miscellaneous.PullToRefreshGridView;
import com.cs48.lethe.utils.NetworkUtilities;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FeedFragment extends Fragment {

    // Logcat tag
    public static final String TAG = FeedFragment.class.getSimpleName();

    // Instance variables
    private FeedGridViewAdapter mFeedGridViewAdapter;

    // Initializations of UI elements
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
     * @param inflater           The LayoutInflater object that can be used
     *                           to inflate any views in the fragment,
     * @param container          If non-null, this is the parent view that the
     *                           fragment's UI should be attached to. The fragment
     *                           should not add the view itself, but this can be
     *                           used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Sets the view to the feed fragment layout
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);

        // Injects the UI elements into the activity
        ButterKnife.inject(this, rootView);

        // Sets up the grid data
        mFeedGridView.setAdapter(mFeedGridViewAdapter);
        mFeedGridView.setExpanded(true);

        // Sets up click and scroll listeners
        mFeedGridView.setOnItemClickListener(new OnPictureClickListener());
        mFeedGridView.setOnScrollListener(new OnScrollListener());
        mFeedPullToRefreshLayout.setOnRefreshListener(new OnRefreshListener());

        fetchFeedFromServer();

        return rootView;
    }

    /**
     * Called when the fragment is visible to the user and actively running. This is
     * generally tied to Activity.onResume of the containing Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();

        // If there is no internet and there is an empty grid,
        // then display network error on grid
        if (!NetworkUtilities.isNetworkAvailable(getActivity())) {
            setEmptyGridMessage(getString(R.string.grid_no_internet_connection));
        } else {
            // Else, if grid is empty, then display empty error on grid
            mFeedGridViewAdapter.fetchFeedFromDatabase();
            setEmptyGridMessage(getString(R.string.grid_area_empty));
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     * You should place your menu items in to menu. For this method to be
     * called, you must have first called setHasOptionsMenu(boolean).
     *
     * @param menu     The options menu in which you place your items.
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

    /**
     * If the grid is empty, then display a error message on the grid.
     * Otherwise, the message is hidden.
     *
     * @param errorMessage The message to display
     * @return True if grid is empty. False otherwise.
     */
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
        // If the network is available then enable the refresh animation
        // and fetch the picture feed from the server
        if (NetworkUtilities.isNetworkAvailable(getActivity())) {
            mFeedPullToRefreshLayout.setRefreshing(true);
            mFeedGridViewAdapter.fetchFeedFromServer(this);
        } else {
            // Else the network is not available, so disable the refresh
            // animation and display a networt alert dialog
            mFeedPullToRefreshLayout.setRefreshing(false);
            if (!setEmptyGridMessage(getString(R.string.grid_no_internet_connection))) {
                try {
                    new NetworkUnavailableAlertDialog().show(getActivity().getFragmentManager(), TAG);
                } catch (IllegalStateException e) {
                    Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Disables the refresh animation
     */
    public void stopRefreshAnimation() {
        mFeedPullToRefreshLayout.setRefreshing(false);
    }

    /**
     * A callback to be invoked when a picture in this AdapterView has been clicked.
     * In other words, this is invoked when a user clicks on a picture in the grid.
     */
    class OnPictureClickListener implements AdapterView.OnItemClickListener {
        /**
         * Callback method to be invoked when picture in this AdapterView has been clicked.
         * This starts the full screen view of the picture.
         *
         * @param parent   The AdapterView where the click happened.
         * @param view     The view within the AdapterView that was clicked
         *                 (this will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id       The row id of the item that was clicked.
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent feedFullScreenIntent = new Intent(getActivity(), FeedFullScreenActivity.class);
            feedFullScreenIntent.putExtra(getString(R.string.data_uniqueId), mFeedGridViewAdapter.getItem(position).getUniqueId());
            startActivity(feedFullScreenIntent);
        }
    }

    /**
     * Callback to be invoked when the grid has been scrolled. This enables and
     * disables the pull-to-refresh feature based upon how the user scrolls.
     */
    class OnScrollListener implements AbsListView.OnScrollListener {
        /**
         * Callback method to be invoked while the list view or grid view is being scrolled.
         * If the view is being scrolled, this method will be called before the next frame
         * of the scroll is rendered. In particular, it will be called before any calls
         * to getView(int, View, ViewGroup).
         *
         * @param view        The view whose scroll state is being reported
         * @param scrollState The current scroll state. One of SCROLL_STATE_TOUCH_SCROLL
         *                    or SCROLL_STATE_IDLE.

         */
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // Enable pull-to-refresh when not scrolling and at the top of the grid
            if (scrollState == SCROLL_STATE_IDLE && view.getChildAt(0).getTop() >= 0)
                mFeedPullToRefreshLayout.setEnabled(true);
            else
            // Else disable pull-to-refresh
                mFeedPullToRefreshLayout.setEnabled(false);
        }

        /**
         * Callback method to be invoked when the list or grid has been scrolled.
         * This will be called after the scroll has completed
         *
         * @param view             The view whose scroll state is being reported
         * @param firstVisibleItem The index of the first visible cell (ignore if visibleItemCount == 0)
         * @param visibleItemCount The number of visible cells
         * @param totalItemCount   The number of items in the list adaptor
         */
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            // If the grid is empty, then allow the pull-to-refresh feature
            if (totalItemCount == 0)
                mFeedPullToRefreshLayout.setEnabled(true);
        }
    }

    /**
     * Callback to be invoked when the grid has been refreshed. In other words,
     * this sets up the refresh listeners for pull-to-refresh feature.
     */
    class OnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        /**
         * Callback method to be invoked when refresh has happened.
         */
        @Override
        public void onRefresh() {
            // Fetches the picture feed from the server
            // and updates the grid
            fetchFeedFromServer();
        }
    }
}
