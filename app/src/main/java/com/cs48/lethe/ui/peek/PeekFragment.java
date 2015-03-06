package com.cs48.lethe.ui.peek;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.cs48.lethe.R;
import com.cs48.lethe.ui.alertdialogs.NetworkUnavailableAlertDialog;
import com.cs48.lethe.ui.miscellaneous.PullToRefreshGridView;
import com.cs48.lethe.utils.NetworkUtilities;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PeekFragment extends Fragment implements OnMapReadyCallback {

    public static final String LOG_TAG = PeekFragment.class.getSimpleName();

    private GoogleMap mMap;
    private PeekGridViewAdapter mPeekGridViewAdapter;
    private String mLatitude;
    private String mLongitude;

    @InjectView(R.id.peekGridView)
    PullToRefreshGridView mPeekGridView;
    @InjectView(R.id.swipeRefreshLayout)
    PeekPullToRefreshLayout mPeekPullToRefreshLayout;
    @InjectView(R.id.addressEditText)
    EditText mAddressEditText;
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
        setRetainInstance(true);

        mPeekGridViewAdapter = new PeekGridViewAdapter(getActivity());
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
     *
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_peek, container, false);

        ButterKnife.inject(this, rootView);

        // Unable to add address or pull-to-refresh until map loads
        mAddressEditText.setEnabled(false);
        mPeekPullToRefreshLayout.setEnabled(false);

        // asyncronously sets up the maps object. the onMapReady will be automatically called
        // below when it is done loading
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        if (!NetworkUtilities.isNetworkAvailable(getActivity()))
            setEmptyGridMessage(getString(R.string.grid_no_internet_connection));
        else
            setEmptyGridMessage("");

        mPeekGridView.setAdapter(mPeekGridViewAdapter);
        mPeekGridView.setOnItemClickListener(new OnPictureClickListener());
        mPeekGridView.setOnScrollListener(new OnScrollListener());
        mPeekPullToRefreshLayout.setOnRefreshListener(new OnRefreshListener());

        /*
         * TODO     Once the user types in the address or drops a pin, you need to
         * TODO     reverse geocode that address to get the latitude and
         * TODO     longitude. Then you can call:
         *
         * TODO     fetchPeekFeedFromServer(latitude, longitude);
         *
         * TODO     and it should show the feed of that area in the grid.
         */

        // Listens for user input on the text box
        mAddressEditText.setOnEditorActionListener(new OnAddressBarEditorActionListener());

        return rootView;
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
            mPeekGridViewAdapter.clearPeekFeed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        android.support.v4.app.FragmentManager fm = getChildFragmentManager();
        SupportMapFragment frag = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (frag != null) {
            fm.beginTransaction().remove(frag).commitAllowingStateLoss();
        }
        super.onDestroyView();

    }

    /**
     * Called when the map is ready to be used.
     *
     * @param googleMap A non-null instance of a GoogleMap associated with the
     *                  MapFragment or MapView that defines the callback.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mAddressEditText.setEnabled(true);
        mPeekPullToRefreshLayout.setEnabled(true);

        String[] coordinates = NetworkUtilities.getCurrentLocation(getActivity());
        mLatitude = coordinates[0];
        mLongitude = coordinates[1];
        double latitude = Double.parseDouble(mLatitude);
        double longitude = Double.parseDouble(mLongitude);

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
    }

    /**
     * If the grid is empty, then display a error message on the grid.
     * Otherwise, the message is hidden.
     *
     * @param errorMessage The message to display
     * @return True if grid is empty. False otherwise.
     */
    public boolean setEmptyGridMessage(String errorMessage) {
        if (mPeekGridViewAdapter.getCount() == 0) {
            mEmptyGridTextView.setVisibility(View.VISIBLE);
            mEmptyGridTextView.setText(errorMessage);
            return true;
        }
        mEmptyGridTextView.setVisibility(View.GONE);
        return false;
    }

    public void stopRefreshingAnimation() {
        mPeekPullToRefreshLayout.setRefreshing(false);
    }

    private void fetchPeekFeedFromServer(String latitude, String longitude) {
        if (NetworkUtilities.isNetworkAvailable(getActivity())) {
            mPeekGridViewAdapter.fetchPeekFeedFromServer(this, latitude, longitude);
        } else {
            mPeekPullToRefreshLayout.setRefreshing(false);
            if (!setEmptyGridMessage(getString(R.string.grid_no_internet_connection))) {
                try {
                    new NetworkUnavailableAlertDialog().show(getActivity().getFragmentManager(), LOG_TAG);
                } catch (IllegalStateException e) {
                    Log.e(LOG_TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Starts the full-screen activity and sends the necessary data to
     * that activity through a Bundle.
     */
    private class OnPictureClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent peekFullScreenIntent = new Intent(getActivity(), PeekFullScreenActivity.class);
            peekFullScreenIntent.putExtra(getString(R.string.data_position), position);
            startActivity(peekFullScreenIntent);
        }
    }

    /**
     * Sets up the scroll listeners for pull-to-refresh on the grid
     */
    private class OnScrollListener implements AbsListView.OnScrollListener {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == SCROLL_STATE_IDLE && view.getChildAt(0).getTop() >= 0)
                mPeekPullToRefreshLayout.setEnabled(true);
            else
                mPeekPullToRefreshLayout.setEnabled(false);
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (totalItemCount == 0)
                mPeekPullToRefreshLayout.setEnabled(true);
        }
    }

    /**
     * Sets up the refresh listeners for pull-to-refresh feature
     */
    private class OnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            /*
            // You need to enable GPS on emulator for this feature to work
            // Sometimes location might be null which results in a crash
            Location location = mMap.getMyLocation();
            mLatitude = location.getLatitude() + "";      // "" converts to String
            mLongitude = location.getLongitude() + "";    // "" converts to String
            fetchPeekFeedFromServer(mLatitude, mLongitude);
            */

            String[] coordinates = NetworkUtilities.getCurrentLocation(getActivity());
            fetchPeekFeedFromServer(coordinates[0], coordinates[1]);
        }
    }

    /**
     * Sets up the input listener for the address textview
     */
    private class OnAddressBarEditorActionListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            Log.d("Okay", "One");
            if (event == null || event.getAction() == KeyEvent.ACTION_DOWN) {
                mPeekPullToRefreshLayout.setRefreshing(true);
                fetchPeekFeedFromServer(mLatitude, mLongitude);
            }

            return false;
        }

    }
}
