package com.cs48.lethe.ui.peek;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import com.cs48.lethe.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by maxkohne on 2/26/15.
 */
public class PeekFullScreenActivity extends ActionBarActivity {

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

        mViewPager.setAdapter(new PeekPagerAdapter(this));

        // Displays the picture at the selected position
        mViewPager.setCurrentItem(getIntent().getIntExtra(getString(R.string.data_position), 0));
    }
}
