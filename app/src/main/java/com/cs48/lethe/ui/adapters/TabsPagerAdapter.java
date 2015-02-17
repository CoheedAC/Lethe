package com.cs48.lethe.ui.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cs48.lethe.ui.fragments.FeedFragment;
import com.cs48.lethe.ui.fragments.MeFragment;
import com.cs48.lethe.ui.fragments.MoreFragment;
import com.cs48.lethe.ui.fragments.PeekFragment;

import java.util.List;
import java.util.Vector;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    private List<String> fragments;
    private Context context;

    /**
     * Addes all of the fragments into the List.
     */
    public TabsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;

        fragments = new Vector<>();
        fragments.add(FeedFragment.class.getName());
        fragments.add(PeekFragment.class.getName());
        fragments.add(MeFragment.class.getName());
        fragments.add(MoreFragment.class.getName());
    }

    /**
     * Returns the fragment at a given index.
     */
    @Override
    public Fragment getItem(int position) {
        return Fragment.instantiate(context, fragments.get(position));
    }

    /**
     * Returns the number of fragments.
     */
    @Override
    public int getCount() {
        return fragments.size();
    }

}
