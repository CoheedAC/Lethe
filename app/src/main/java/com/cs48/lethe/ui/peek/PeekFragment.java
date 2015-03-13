package com.cs48.lethe.ui.peek;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * This fragment contains the interactive Google map, and it is made to display
 * a grid view of all of the photos from the region selected by the user on the
 * map. This class enables the user to manually enter in any address, or click on
 * any point on the map and as a result, view the corresponding photos from
 * within a 5 mile radius of where he clicked.
 */

public class PeekFragment extends Fragment implements OnMapReadyCallback {

    public static final String TAG = PeekFragment.class.getSimpleName();

    private GoogleMap mMap;
    private PeekGridViewAdapter mPeekGridViewAdapter;
    private Location mCurrentLocation;
    private Marker mMarker;
    private int mMapZoom = 14;

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
     * setRetainInstance(true) enables the fragment instance to be retained
     * across activity recreation. The member variable mPeekGridViewAdapter is
     * initialized to a new PeekGridViewAdapter.
     *
     * @param savedInstanceState If the fragment is being re-created from a
     *                           previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mPeekGridViewAdapter = new PeekGridViewAdapter(getActivity(), this);
    }

    /**
     * Called to have the fragment instantiate its user interface view. This
     * will be called between onCreate(Bundle) and onActivityCreated(Bundle).
     * If you return a View from here, you will later be called in
     * onDestroyView() when the view is being released.
     *
     * This method makes it so you are unable to add address or pull-to-refresh until the
     * map loads. The Map object is also asynchronously set-up. The grid view and address
     * entry event listeners are set up.
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
        View rootView = inflater.inflate(R.layout.fragment_peek, container, false);

        ButterKnife.inject(this, rootView);

        mAddressEditText.setEnabled(false);

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
        mAddressEditText.setOnEditorActionListener(new OnAddressBarEditorActionListener());

        return rootView;
    }

    /**
     * Deletes the map fragment.
     */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment frag = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (frag != null)
            fm.beginTransaction().remove(frag).commitAllowingStateLoss();
    }

    /**
     * Whenever the map fragment is resumed, the data is fetched from the server again.
     */

    @Override
    public void onResume() {
        super.onResume();
        mPeekGridViewAdapter.fetchPeekFeedFromDatabase();
    }

    /**
     * Called when the map is ready to be used. Initialized the map location and a map marker
     * to the current location of the user. All map gestures (zooming, scrolling, tilting,
     * etc...) are enabled. Makes it so when the user inputs an address, the camera moves
     * to the inputted address.
     *
     * @param googleMap A non-null instance of a GoogleMap associated with the
     *                  MapFragment or MapView that defines the callback.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new OnCurrentLocationButtonClick());
        mMap.setOnMapClickListener(new OnMapClick());
        mMap.setOnMyLocationChangeListener(new OnLocationChange());
        mMap.setOnCameraChangeListener(new OnZoomChange());
        mAddressEditText.setEnabled(true);
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

    /**
     * Sets `refreshing` to false.
     */

    public void stopRefreshingAnimation() {
        mPeekPullToRefreshLayout.setRefreshing(false);
    }

    /**
     * This method uses the inputted latitude and longitude to fetch the
     * corresponding photos from the server. Checks if the network is
     * available and if it isn't, it displays an error message.
     *
     * @param latitude The latitude from where the user requests photos.
     * @param longitude The longitude from where the user requests photos.
     */

    private void fetchPeekFeedFromServer(double latitude, double longitude) {
        if (NetworkUtilities.isNetworkAvailable(getActivity())) {
            mPeekGridViewAdapter.fetchPeekFeedFromServer(latitude, longitude);
        } else {
            mPeekPullToRefreshLayout.setRefreshing(false);
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
     * Hides the keyboard.
     */

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mAddressEditText.getWindowToken(), 0);
    }

    /**
     * Removes the marker from the screen (makes it null). To do this, it checks to
     * see if the marker is null; if it isn't, it deletes it.
     */

    private void removeMarker() {
        if (mMarker != null) {
            mMarker.remove();
            mMarker = null;
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
     * Sets up the refresh listeners for pull-to-refresh feature.
     */
    private class OnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            hideKeyboard();
            if (mMarker != null) {
                LatLng latLng = mMarker.getPosition();
                fetchPeekFeedFromServer(latLng.latitude, latLng.longitude);
            } else if (mCurrentLocation != null)
                fetchPeekFeedFromServer(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }
    }

    /**
     * Sets up the input listener for the address textview
     */
    private class OnAddressBarEditorActionListener implements TextView.OnEditorActionListener {

        /**
         * When the user enters in an address this method uses a Geocoder object to return a list
         * of the address matches containing the proper latitude, longitude, and other information
         * about the location.
         * When the user clicks enter:
         *  -the map camera relocates the the location of the 1st address returned by the geocoder
         *      object.
         *  -a marker appears on the map that has an info window with the name of the city
         *      that it is located in.
         *
         * @param v The textview that the user clicked
         * @param actionId An int of the actionID
         * @param event A KeyEvent that reports the button event.
         * @return Returns true if user clicked enter or the event was null. False otherwise.
         */
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (event == null || event.getAction() == KeyEvent.ACTION_DOWN) {
                try {
                    Geocoder geocoder = new Geocoder(getActivity());
                    List<Address> addressList = geocoder.getFromLocationName(mAddressEditText.getText().toString(), 2);
                    Address address = addressList.get(0);

                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    fetchPeekFeedFromServer(latLng.latitude, latLng.longitude);

                    mAddressEditText.setText(address.getAddressLine(0) + ", " + address.getAddressLine(1));

                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, mMapZoom, 0, 0)));

                    if (mMarker == null)
                        mMarker = mMap.addMarker(new MarkerOptions().position(latLng));
                    mMarker.setPosition(latLng);
                    mMarker.setTitle(address.getLocality());
                    mMarker.showInfoWindow();

                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    mPeekGridViewAdapter.clearPeekFeed();
                    removeMarker();
                } finally {
                    hideKeyboard();
                }
            }
            return false;
        }
    }

    /**
     * Class for method that is called when the camera changes position.
     */

    private class OnZoomChange implements GoogleMap.OnCameraChangeListener {
        /**
         * When the camera's position changes, this method zooms in to where
         * the camera is.
         * @param cameraPosition CameraPosition input that contains all of the
         * camera's current position parameters.
         */

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            mMapZoom = (int) cameraPosition.zoom;
        }
    }

    /**
     * Class that contains the behavior that occurs when the user clicks on
     * the map.
     */

    private class OnMapClick implements GoogleMap.OnMapClickListener{

        /**
         * This method fetches photos from the server that are within a 5 mile radius (which is specified
         * on the server side) of where the user clicked on the map.
         * A geocoder object is also used to
         * obtain the address information of the latitude and longitude located where the user clicked.
         * A marker is dropped where the user clicks that displays the city information of
         * that area.
         * The map camera is also always updated so that the point where the marker has been dropped is
         * always in the center of the screen.
         *
         * @param latLng The object of the latitude and longitude information
         * of where the user clicked on the map.
         */

        @Override
        public void onMapClick(LatLng latLng) {
            mAddressEditText.setText("");

            if (mMarker == null)
                mMarker = mMap.addMarker(new MarkerOptions().position(latLng));

            Geocoder geocoder = new Geocoder(getActivity());
            try {
                List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, mMapZoom, 0, 0)), 200, null);
                mMarker.setPosition(latLng);
                mMarker.setTitle(addressList.get(0).getLocality());
                mMarker.showInfoWindow();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IndexOutOfBoundsException e) {
                mPeekGridViewAdapter.clearPeekFeed();
                mMarker.hideInfoWindow();
            }
            hideKeyboard();

            fetchPeekFeedFromServer(latLng.latitude, latLng.longitude);
        }
    }

    /**
     * Class that updates the camera according to the location.
     */

    public class OnLocationChange implements GoogleMap.OnMyLocationChangeListener {

        /**
         * If either mCurrentlocation or mMarker are null, then the camera is updated to where
         * the latitude and longitdue of the location object is.
         *
         * @param location Location object that contains location information.
         */
        @Override
        public void onMyLocationChange(Location location) {
            if (mCurrentLocation == null || mMarker == null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, 14, 0, 0)), 200, null);
            }
            mCurrentLocation = location;
        }
    }


    /**
     * Class that implements the interface for when the user clicks on the current location button.
     */
    public class OnCurrentLocationButtonClick implements GoogleMap.OnMyLocationButtonClickListener {

        /**
         * Upon click of the current location button, the text from mAddressEditText is hidden and
         * the GridView of photos below the map is cleared.
         *
         * @return Returns false upon completion.
         */
        @Override
        public boolean onMyLocationButtonClick() {
            mAddressEditText.setText("");
            hideKeyboard();
            mPeekGridViewAdapter.clearPeekFeed();
            setEmptyGridMessage("");
            removeMarker();
            return false;
        }
    }
}
