package com.cs48.lethe.ui.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import com.cs48.lethe.R;
import com.cs48.lethe.ui.adapters.MePagerAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MeFullScreenActivity extends ActionBarActivity {

    private MePagerAdapter mMePagerAdapter;
    private int mPosition;

    @InjectView(R.id.pager)
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        ButterKnife.inject(this);

        // Hides the action bar
        getSupportActionBar().hide();

        // Gets the position passed in the the intent
        mPosition = getIntent().getIntExtra(getString(R.string.data_position), 0);

        mMePagerAdapter = new MePagerAdapter(this);
        mViewPager.setAdapter(mMePagerAdapter);

        // Displays the picture at the selected position
        mViewPager.setCurrentItem(mPosition);
    }

}
