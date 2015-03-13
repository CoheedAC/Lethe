package com.cs48.lethe.ui.me;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import com.cs48.lethe.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * The activity that handles showing the full-sized image
 * whenever the user tabs on an image in the grid of user-posted images.
 */
public class MeFullScreenActivity extends ActionBarActivity {

    @InjectView(R.id.pager)
    ViewPager mViewPager;

    /**
     * Called when the activity is starting. This is where most initialization should go:
     * calling setContentView(int) to inflate the activity's UI, using findViewById(int)
     * to programmatically interact with widgets in the UI, and initializing other variables
     * that need to be set.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets the view to the full screen layout
        setContentView(R.layout.activity_fullscreen);

        // Injects the UI elements into the activity
        ButterKnife.inject(this);

        // Hides the action bar
        getSupportActionBar().hide();

        // Sets the adapter that allows a slideshow of pictures
        mViewPager.setAdapter(new MePagerAdapter(this));

        // Displays the picture at the selected position
        mViewPager.setCurrentItem(getIntent().getIntExtra(getString(R.string.data_position), 0));
    }

}
