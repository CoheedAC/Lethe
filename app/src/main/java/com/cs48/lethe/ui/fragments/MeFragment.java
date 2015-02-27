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
import com.cs48.lethe.ui.activities.MeFullScreenActivity;
import com.cs48.lethe.ui.adapters.MeGridViewAdapter;
import com.cs48.lethe.utils.ActionCodes;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MeFragment extends Fragment {

    public static final String LOG_TAG = MeFragment.class.getSimpleName();

    private MeGridViewAdapter mMeGridViewAdapter;

    @InjectView(R.id.meGridView)
    GridView mMeGridView;

    /**
     * Sets up the action bar menu.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mMeGridViewAdapter = new MeGridViewAdapter(getActivity());
    }

    /**
     * Sets up the grid and handles the image click event.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_me, container, false);

        ButterKnife.inject(this, rootView);

        mMeGridView.setAdapter(mMeGridViewAdapter);
        mMeGridView.setOnItemClickListener(new OnPictureClickListener());

        return rootView;
    }

    /**
     * Hides the refresh and the clear cache buttons.
     */
    @Override
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
            mMeGridViewAdapter.deleteAllPostedImages();
            return true;
        }

        /**
         * Copies the first image 50 times to create
         * a dummy grid for testing purposes.
         */
        if (id == R.id.action_copy_images) {
            mMeGridViewAdapter.copyFirstImage();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles actions when a requested activity is finished.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ActionCodes.ME_FULLSCREEN_REQUEST && resultCode == ActionCodes.DELETE_PICTURE)
            fetchMePicturesFromDatabase();

    }

    /**
     * Gets the list of posted pictures from the database.
     */
    public void fetchMePicturesFromDatabase() {
        mMeGridViewAdapter.fetchMePicturesFromDatabase();
    }

    class OnPictureClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent meFullPictureIntent = new Intent(getActivity(), MeFullScreenActivity.class);
            meFullPictureIntent.putExtra(getString(R.string.data_position), position);
            startActivityForResult(meFullPictureIntent, ActionCodes.ME_FULLSCREEN_REQUEST);
        }
    }
}
