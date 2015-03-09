package com.cs48.lethe.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.cs48.lethe.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MeFragment extends Fragment {

    public static final String TAG = MeFragment.class.getSimpleName();

    private MeGridViewAdapter mMeGridViewAdapter;

    @InjectView(R.id.meGridView)
    GridView mMeGridView;
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

        mMeGridViewAdapter = new MeGridViewAdapter(getActivity());
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
     * @return Return the View for the fragment's UI, or nullnt.
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

    /**
     * Called when the fragment is visible to the user and actively running. This is
     * generally tied to Activity.onResume of the containing Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();
        fetchMePicturesFromDatabase();
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

        /**
         * A callback to be invoked when an item in this AdapterView has been clicked.
         */}

    /**
     * A callback to be invoked when an item in this AdapterView has been clicked.
     */
    class OnPictureClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent meFullPictureIntent = new Intent(getActivity(), MeFullScreenActivity.class);
            meFullPictureIntent.putExtra(getString(R.string.data_position), position);
            startActivity(meFullPictureIntent);
        }
    }
}
