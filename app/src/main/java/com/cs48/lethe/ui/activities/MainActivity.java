package com.cs48.lethe.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.cs48.lethe.R;
import com.cs48.lethe.ui.adapters.TabsPagerAdapter;
import com.cs48.lethe.ui.fragments.FeedFragment;
import com.cs48.lethe.ui.fragments.MeFragment;
import com.cs48.lethe.ui.fragments.MoreFragment;
import com.cs48.lethe.ui.fragments.PeekFragment;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener,
        FeedFragment.OnFragmentInteractionListener, PeekFragment.OnFragmentInteractionListener,
        MeFragment.OnFragmentInteractionListener, MoreFragment.OnFragmentInteractionListener {

    // For debugging purposes
    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private TabsPagerAdapter mTabsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setTitle("Home");
        setUpActionBar();
    }

    private void setUpActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Show app icon
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.ic_launcher);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager(), this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTabsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        final int[] ICONS = new int[]{
                R.drawable.ic_action_picture,
                R.drawable.ic_action_map,
                R.drawable.ic_action_person,
                R.drawable.ic_action_share
        };

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mTabsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
//                            .setText(mTabsPagerAdapter.getPageTitle(i))
                            .setIcon(ICONS[i])
                            .setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.getItem(0).setVisible(false);
        menu.getItem(1).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Starts camera activity
        if (id == R.id.action_camera) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        int position = tab.getPosition();

        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(position);

        // Changes title based upon which tab is selected
        switch (position) {
            case 0:
                setTitle(R.string.title_section1);
                break;
            case 1:
                setTitle(R.string.title_section2);
                break;
            case 2:
                setTitle(R.string.title_section3);
                break;
            case 3:
                setTitle(R.string.title_section4);
                break;
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}