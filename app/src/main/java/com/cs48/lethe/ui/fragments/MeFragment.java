package com.cs48.lethe.ui.fragments;

import android.content.Intent;
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
import com.cs48.lethe.ui.activities.MeFullPictureActivity;
import com.cs48.lethe.ui.adapters.MeGridAdapter;
import com.cs48.lethe.utils.ActionCodes;

public class MeFragment extends Fragment {

    public static final String LOG_TAG = MeFragment.class.getSimpleName();

    private MeGridAdapter mGridAdapter;
    private GridView mGridView;

    /**
     * Sets up the action bar menu.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

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
        mGridAdapter = new MeGridAdapter(getActivity());
        mGridView.setAdapter(mGridAdapter);

        /**
         * Starts the full-screen activity and sends the necessary data to
         * that activity through a Bundle.
         */
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent meFullPictureIntent = new Intent(getActivity(), MeFullPictureActivity.class);

                meFullPictureIntent.putExtra("position", position);

                startActivityForResult(meFullPictureIntent, ActionCodes.ME_FULL_PICTURE_REQUEST);
            }
        });

        return rootView;
    }

    /**
     * Hides the refresh and the clear cache buttons.
     */
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_clear_cache).setVisible(true);
        menu.findItem(R.id.action_copy_images).setVisible(true);
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
        if (id == R.id.action_clear_cache) {
            mGridAdapter.deleteAllPostedImages();
            return true;
        }

        /**
         * Copies the first image 50 times to create
         * a dummy grid for testing purposes.
         */
        if (id == R.id.action_copy_images) {
            mGridAdapter.copyFirstImage();
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Handles actions when a requested activity is finished.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ActionCodes.ME_FULL_PICTURE_REQUEST && resultCode == ActionCodes.DELETE_PICTURE)
            fetchMePicturesFromDatabase();

    }

    public void fetchMePicturesFromDatabase() {
        mGridAdapter.fetchMePicturesFromDatabase();
    }

}
