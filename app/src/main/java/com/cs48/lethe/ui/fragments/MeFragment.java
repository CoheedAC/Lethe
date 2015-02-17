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

import com.cs48.lethe.R;
import com.cs48.lethe.ui.activities.FullScreenImageActivity;
import com.cs48.lethe.ui.adapters.MeGridViewAdapter;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeFragment extends Fragment {

    private MeGridViewAdapter mGridAdapter;
    private GridView mGridView;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MeFragment.
     */
    public static MeFragment newInstance() {
        return new MeFragment();
    }

    public MeFragment() {
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
     * Updates the grid.
     */
    public void update() {
        mGridAdapter.update();
    }

    /**
     * Sets up the grid and handles the image click event.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_me, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.meGridView);
        mGridAdapter = new MeGridViewAdapter(getActivity());
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
                showImageIntent.setAction(FullScreenImageActivity.VIEW_OVERLAY);

                startActivityForResult(showImageIntent, FullScreenImageActivity.FULL_IMAGE_REQUEST);
            }
        });

        return rootView;
    }

    /**
     * Hides the refresh and the clear cache buttons.
     */
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.getItem(2).setVisible(true);
        menu.getItem(1).setVisible(true);
    }

    /**
     * Handles the action bar button press events.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /**
         * Deletes all the images stored on the device for
         * testing purposes.
         */
        if (id == R.id.action_delete_images) {
            mGridAdapter.deleteAllImages();
            return true;
        }

        /**
         * Copies the first image 50 times to create
         * a dummy grid for testing purposes.
         */
        if (id == R.id.action_copy_images) {
            mGridAdapter.copyImage();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles actions when a requested activity is finished.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // updates the grid if the user deletes the image in the full screen view
        if (requestCode == FullScreenImageActivity.FULL_IMAGE_REQUEST && resultCode == FullScreenImageActivity.RESULT_OK) {
            update();
        }
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
