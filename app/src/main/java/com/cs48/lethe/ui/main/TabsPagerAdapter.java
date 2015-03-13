package com.cs48.lethe.ui.main;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cs48.lethe.ui.feed.FeedFragment;
import com.cs48.lethe.ui.me.MeFragment;
import com.cs48.lethe.ui.more.MoreFragment;
import com.cs48.lethe.ui.peek.PeekFragment;

import java.util.List;
import java.util.Vector;

/**
 * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    // Instance variables
    private List<String> fragments;
    private Context mContext;

    /**
     * Implementation of PagerAdapter that represents each page as a
     * Fragment that is persistently kept in the fragment manager as
     * long as the user can return to the page.
     *
     * @param fragmentManager The fragment manager
     * @param context Interface to global information about an application environment
     */
    public TabsPagerAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        mContext = context;

        fragments = new Vector<>();
        fragments.add(FeedFragment.class.getName());
        fragments.add(PeekFragment.class.getName());
        fragments.add(MeFragment.class.getName());
        fragments.add(MoreFragment.class.getName());
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position The desired position of the fragment
     *
     * @return The Fragment associated with a specified position.
     */
    @Override
    public Fragment getItem(int position) {
        return Fragment.instantiate(mContext, fragments.get(position));
    }

    /**
     * @return Return the number of views available.
     */
    @Override
    public int getCount() {
        return fragments.size();
    }
}
