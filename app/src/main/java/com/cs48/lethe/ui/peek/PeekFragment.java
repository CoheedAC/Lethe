package com.cs48.lethe.ui.peek;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class PeekFragment extends Fragment implements OnMapReadyCallback {

    public static final String TAG = PeekFragment.class.getSimpleName();

    private GoogleMap mMap;
    private PeekGridViewAdapter mPeekGridViewAdapter;
    private double mLatitude;
    private double mLongitude;
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

        // Unable to add address or pull-to-refresh until map loads
        mAddressEditText.setEnabled(false);

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
        mAddressEditText.setOnEditorActionListener(new OnAddressBarEditorActionListener());

        return rootView;
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
     * Called when the map is ready to be used. Initialized the map location and a map marker
     * to the current location of the user
     *
     * @param googleMap A non-null instance of a GoogleMap associated with the
     *                  MapFragment or MapView that defines the callback.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mAddressEditText.setEnabled(true);

        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(true);

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mAddressEditText.setText("");
                hideKeyboard();
                mPeekGridViewAdapter.clearPeekFeed();
                setEmptyGridMessage("");
                if (mMarker != null)
                    mMarker.remove();
                mMarker = null;
                return false;
            }
        });
        mMap.setOnMapClickListener(new OnMapClick());
        mMap.setOnMyLocationChangeListener(new OnLocationChange());
        mMap.setOnCameraChangeListener(new OnZoomChange());
    }

    private void setMarkerPosition(LatLng latLng) {
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
            hideKeyboard();
            fetchPeekFeedFromServer(mLatitude, mLongitude);
        }
    }

    /**
     * Sets up the input listener for the address textview
     */
    private class OnAddressBarEditorActionListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (event == null || event.getAction() == KeyEvent.ACTION_DOWN) {
                try {
                    Geocoder geocoder = new Geocoder(getActivity());
                    List<Address> addressList = geocoder.getFromLocationName(mAddressEditText.getText().toString(), 2);
                    Address address = addressList.get(0);
                    mLongitude = address.getLongitude();
                    mLatitude = address.getLatitude();

                    fetchPeekFeedFromServer(mLatitude, mLongitude);

                    String fullAddress = address.getAddressLine(0) + " " + address.getAddressLine(1);
                    mAddressEditText.setText(fullAddress);
                    LatLng latLng = new LatLng(mLatitude, mLongitude);

                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, mMapZoom, 0, 0)));

                    if (mMarker == null)
                        mMarker = mMap.addMarker(new MarkerOptions().position(latLng));
                    mMarker.setPosition(latLng);
                    mMarker.setTitle(address.getLocality());
                    mMarker.hideInfoWindow();

                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    mPeekGridViewAdapter.clearPeekFeed();
                    if (mMarker != null)
                        mMarker.hideInfoWindow();
                } finally {
                    hideKeyboard();
                }
            }
            return false;
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mAddressEditText.getWindowToken(), 0);
    }

    private class OnZoomChange implements GoogleMap.OnCameraChangeListener {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            mMapZoom = (int) cameraPosition.zoom;
        }
    }

    private class OnMapClick implements GoogleMap.OnMapClickListener {
        @Override
        public void onMapClick(LatLng latLng) {
            mLatitude = latLng.latitude;
            mLongitude = latLng.longitude;
            mAddressEditText.setText("");

            if (mMarker == null)
                mMarker = mMap.addMarker(new MarkerOptions().position(latLng));

            setMarkerPosition(latLng);
            fetchPeekFeedFromServer(mLatitude, mLongitude);
        }
    }

    public class OnLocationChange implements GoogleMap.OnMyLocationChangeListener {
        @Override
        public void onMyLocationChange(Location location) {
            if (mCurrentLocation == null) {
                mCurrentLocation = location;
                mLatitude = mCurrentLocation.getLatitude();
                mLongitude = mCurrentLocation.getLongitude();
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, 14, 0, 0)), 200, null);
            }
        }
    }
}
