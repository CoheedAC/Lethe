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
import android.widget.TextView;

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
    @InjectView(R.id.emptyGridTextView)
    TextView mEmptyGridTextView;

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

        setEmptyGridMessage(getString(R.string.grid_no_posted_pictures));

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchMePicturesFromDatabase();
    }

    /**
     * Hides the refresh and the clear cache buttons.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_clear_cache).setVisible(true);
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
            setEmptyGridMessage(getString(R.string.grid_no_posted_pictures));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setEmptyGridMessage(String errorMessage) {
        if (mMeGridViewAdapter.getCount() == 0) {
            mEmptyGridTextView.setVisibility(View.VISIBLE);
            mEmptyGridTextView.setText(errorMessage);
        } else {
            mEmptyGridTextView.setVisibility(View.GONE);
        }
    }

    /**
     * Gets the list of posted pictures from the database.
     */
    public void fetchMePicturesFromDatabase() {
        mMeGridViewAdapter.fetchMePicturesFromDatabase();
        setEmptyGridMessage(getString(R.string.grid_no_posted_pictures));
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
