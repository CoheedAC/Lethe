package com.cs48.lethe.ui.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import com.cs48.lethe.R;
import com.cs48.lethe.ui.adapters.PeekPagerAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by maxkohne on 2/26/15.
 */
public class PeekFullScreenActivity extends ActionBarActivity {

    private PeekPagerAdapter mPeekPagerAdapter;
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

        mPeekPagerAdapter = new PeekPagerAdapter(this);
        mViewPager.setAdapter(mPeekPagerAdapter);

        // Displays the picture at the selected position
        mViewPager.setCurrentItem(mPosition);
    }
}
