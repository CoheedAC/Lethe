package com.cs48.lethe.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.cs48.lethe.ui.activities.PeekFullScreenActivity;
import com.cs48.lethe.ui.adapters.PeekGridViewAdapter;
import com.cs48.lethe.ui.view_helpers.PeekPullToRefreshLayout;
import com.cs48.lethe.ui.view_helpers.PullToRefreshGridView;
import com.cs48.lethe.utils.ActionCodes;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        mPeekGridViewAdapter = new PeekGridViewAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_peek, container, false);

        ButterKnife.inject(this, rootView);

        // Gets current location just to test the grid (defaults to IV lat and long)
        String[] coordinates = NetworkUtilities.getCurrentLocation(getActivity());
        mLatitude = coordinates[0];
        mLongitude = coordinates[1];

        // asyncronously sets up the maps object. the onMapReady will be automatically called
        // below when it is done loading

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        mPeekGridView.setAdapter(mPeekGridViewAdapter);
        mPeekGridView.setOnItemClickListener(new OnPictureClickListener());
        mPeekGridView.setOnScrollListener(new OnScrollListener());
        mPeekPullToRefreshLayout.setOnRefreshListener(new OnRefreshListener());

        /**
         * Once you get the map set up, you need to reverse geocode what the user
         * typed in or where they dropped the pin to get the latitude
         * and longitude. Once you get that information, just type in:
         *
         * mLatitude = // reverse geocoded latitude
         * mLongitude = // reverse geocoded longitude
         * mPeekGridAdapter.fetchPeekFeedFromServer(mLatitude, mLongitude)
         *
         * and it should show the feed of that area in the grid
         */

        //  Tells the grid adapter to fetch the feed from the server with the given coords
//        mPeekGridViewAdapter.fetchPeekFeedFromServer(mLatitude, mLongitude);

        // Listens for user input on the text box
        mAddressEditText.setOnEditorActionListener(new OnTextEditListener());

        return rootView;
    }

    /**
     * Hides the delete all images and copy image button in the action bar.
     */
    @Override
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
            mPeekGridViewAdapter.clearPeekFeed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        double latitude = Double.parseDouble(mLatitude);
        double longitude = Double.parseDouble(mLongitude);
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
    }

    /**
     * Starts the full-screen activity and sends the necessary data to
     * that activity through a Bundle.
     */
    class OnPictureClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent peekFullScreenIntent = new Intent(getActivity(), PeekFullScreenActivity.class);
            peekFullScreenIntent.putExtra(getString(R.string.data_position), position);
            startActivityForResult(peekFullScreenIntent, ActionCodes.PEEK_FULLSCREEN_REQUEST);
        }
    }

    /**
     * Sets up the scroll listeners for pull-to-refresh on the grid
     */
    class OnScrollListener implements AbsListView.OnScrollListener {
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
    class OnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            if (mLatitude != null && mLongitude != null)
                mPeekGridViewAdapter.fetchPeekFeedFromServer(mPeekPullToRefreshLayout, mLatitude, mLongitude);
        }
    }

    /**
     * Sets up the input listener for the edit textview
     */
    class OnTextEditListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN)
                mPeekGridViewAdapter.fetchPeekFeedFromServer(mLatitude, mLongitude);
            return false;
        }
    }
    @Override
    public void onDestroyView(){
        android.support.v4.app.FragmentManager fm = getChildFragmentManager();
        SupportMapFragment frag = (SupportMapFragment)fm.findFragmentById(R.id.map);
        if(frag != null){
            fm.beginTransaction().remove(frag).commitAllowingStateLoss();
        }
        super.onDestroyView();

    }

}
