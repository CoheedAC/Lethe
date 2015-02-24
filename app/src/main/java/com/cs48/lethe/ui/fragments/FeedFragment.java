package com.cs48.lethe.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.ui.activities.FullPictureActivity;
import com.cs48.lethe.ui.adapters.FeedGridViewAdapter;
import com.cs48.lethe.utils.Image;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedFragment extends Fragment {

    public static final String TAG = FeedFragment.class.getSimpleName();

    private FeedGridViewAdapter mGridAdapter;
    private GridView mGridView;

    private OnFragmentInteractionListener mListener;

    public static FeedFragment newInstance() {
        FeedFragment fragment = new FeedFragment();
        return fragment;
    }

    public FeedFragment() {
        // Required empty public constructor
    }

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
        mGridView = (GridView) rootView.findViewById(R.id.feedGridView);

        mGridAdapter = new FeedGridViewAdapter(getActivity());
        mGridView.setAdapter(mGridAdapter);

        /**
         * Starts the full-screen activity and sends the necessary data to
         * that activity through a Bundle.
         */
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent showImageIntent = new Intent(getActivity(), FullPictureActivity.class);

                Image image = (Image) mGridAdapter.getItem(position);
                showImageIntent.putExtra("uniqueId", image.getUniqueId());
                showImageIntent.putExtra("position", position);
                showImageIntent.setAction(FullPictureActivity.CACHED_IMAGE_INTERFACE);

                startActivityForResult(showImageIntent, FullPictureActivity.FULL_PICTURE_REQUEST);
            }
        });

        /**
         * UNIMPLEMENTED
         * Scroll state for pull-to-refresh implementation
         */
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        return rootView;
    }

    /**
     * Hides the delete all images and copy image button in the action bar.
     */
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_delete_images).setVisible(true);
        menu.findItem(R.id.action_refresh).setVisible(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FullPictureActivity.FULL_PICTURE_REQUEST) {
            if (resultCode == FullPictureActivity.HIDDEN) {
                data.getIntExtra("position", -1);
                mGridAdapter.hideImageFromFeed(data.getIntExtra("position", -1));
            } else {
                mGridAdapter.updateImageStatistics(data.getIntExtra("position", -1));
            }
        }
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
        if (id == R.id.action_delete_images) {
            Toast.makeText(getActivity(), "Cleared cache", Toast.LENGTH_SHORT).show();
            mGridAdapter.clearCache();
            return true;
        }

        /**
         * Requests to get new images on the server.
         */
        if (id == R.id.action_refresh) {
            Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
            mGridAdapter.fetchFeedFromServer();
        }
        return super.onOptionsItemSelected(item);
    }

    public void fetchFeedFromServer() {
        mGridAdapter.fetchFeedFromServer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    public void onGridItemClick(GridView g, View v, int position, long id) {
        // Send the event to the host activity
        mListener.onGridItemSelected(position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onGridItemSelected(int position);
    }

}
