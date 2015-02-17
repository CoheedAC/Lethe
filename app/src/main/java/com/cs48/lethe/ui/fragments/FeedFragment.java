package com.cs48.lethe.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.ui.activities.FullScreenImageActivity;
import com.cs48.lethe.ui.adapters.FeedGridViewAdapter;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedFragment extends Fragment {

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
                Intent showImageIntent = new Intent(getActivity(), FullScreenImageActivity.class);

                File imageFile = (File) mGridAdapter.getItem(position);
                showImageIntent.setData(Uri.fromFile(imageFile));
                showImageIntent.putExtra("position", position);
                showImageIntent.setAction(FullScreenImageActivity.VIEW_ONLY);

                startActivity(showImageIntent);
            }
        });
        return rootView;
    }

    /**
     * Updates the grid.
     */
    public void update() {
        mGridAdapter.update();
    }

    /**
     * Hides the delete all images and copy image button in the action bar.
     */
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.getItem(0).setVisible(true);
        menu.getItem(2).setVisible(true);
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
            mGridAdapter.requestFeed();
            Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        public void onFragmentInteraction(Uri uri);
    }

}
