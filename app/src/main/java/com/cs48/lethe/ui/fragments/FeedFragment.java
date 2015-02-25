package com.cs48.lethe.ui.fragments;

import android.app.Activity;
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
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.ui.activities.FullPictureActivity;
import com.cs48.lethe.ui.adapters.FeedGridViewAdapter;
import com.cs48.lethe.ui.view_helpers.ExpandableHeightGridView;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.Image;
import com.cs48.lethe.ui.view_helpers.ScrollableSwipeRefreshLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedFragment extends Fragment {

    public static final String LOG_TAG = FeedFragment.class.getSimpleName();

    private FeedGridViewAdapter mGridAdapter;

    @InjectView(R.id.feedGridView)
    ExpandableHeightGridView mGridView;
    @InjectView(R.id.swipeRefreshLayout)
    ScrollableSwipeRefreshLayout mSwipeRefreshLayout;

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

        ButterKnife.inject(this, rootView);

        pullToRefreshSetUp();

        mGridView.setExpanded(true);
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

        return rootView;
    }

    /**
     * Sets up the listeners for pull-to-refresh on the grid
     */
    private void pullToRefreshSetUp() {

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE && view.getChildAt(0).getTop() >= 0)
                    mSwipeRefreshLayout.setEnabled(true);
                else
                    mSwipeRefreshLayout.setEnabled(false);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount == 0)
                    mSwipeRefreshLayout.setEnabled(true);
            }

        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FileUtilities.logResults(getActivity(), LOG_TAG, "Refreshing...");
                mGridAdapter.fetchFeedFromServer(mSwipeRefreshLayout);
            }
        });

    }

    /**
     * Hides the delete all images and copy image button in the action bar.
     */
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_delete_images).setVisible(true);
    }

    /**
     * Hides the image from the feed or updates the database with the new
     * likes and views when returning from the full screen activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FullPictureActivity.FULL_PICTURE_REQUEST) {
            if (resultCode == FullPictureActivity.HIDDEN) {
                data.getIntExtra("position", -1);
                mGridAdapter.hideImage(data.getIntExtra("position", -1));
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

        return super.onOptionsItemSelected(item);
    }

    public void fetchFeedFromServer() {
        mGridAdapter.fetchFeedFromServer(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

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
