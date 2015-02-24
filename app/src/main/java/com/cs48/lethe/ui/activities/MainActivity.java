package com.cs48.lethe.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

/**
 * The main activity where the app launches. It handles all of the tab fragments
 * and action bar button presses.
 */
public class MainActivity extends ActionBarActivity implements ActionBar.TabListener,
        FeedFragment.OnFragmentInteractionListener, PeekFragment.OnFragmentInteractionListener,
        MeFragment.OnFragmentInteractionListener, MoreFragment.OnFragmentInteractionListener {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private TabsPagerAdapter mTabsPagerAdapter;
    private ViewPager mViewPager;

    /**
     * Creates the action bar and title.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Home");
        setUpActionBar();
    }

    /**
     * Sets up the custom action bar with the tabs (and the respective
     * tab listeners/handlers).
     */
    private void setUpActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Show app icon
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.ic_launcher);

        // Create the adapter that will return a fragment for each of the four
        // primary sections of the activity.
        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager(), this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTabsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

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
                            .setIcon(ICONS[i])
                            .setTabListener(this));
        }
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_copy_images).setVisible(false);
        menu.findItem(R.id.action_delete_images).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        return true;
    }

    /**
     * Handles button presses on action bar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Starts camera activity if camera button pressed
        if (id == R.id.action_camera) {
            Intent cameraIntent = new Intent(this, CameraActivity.class);
            startActivityForResult(cameraIntent, CameraActivity.IMAGE_POST_REQUEST);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CameraActivity.IMAGE_POST_REQUEST && resultCode == CameraActivity.POST_SUCCESS) {
            FeedFragment feedFragment = (FeedFragment) findFragmentByPosition(0);
            feedFragment.fetchFeedFromServer();

            MeFragment meFragment = (MeFragment) findFragmentByPosition(2);
            meFragment.fetchImagesFromDatabase();
        }
    }

    public Fragment findFragmentByPosition(int position) {
        return getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + mViewPager.getId() + ":"
                        + mTabsPagerAdapter.getItemId(position));
    }

    /**
     * Changes the title on the action bar when a new tab is selected.
     */
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
    public void onGridItemSelected(int position) {
        // The user selected the headline of an article from the HeadlinesFragment
        // Do something here to display that article
        Fragment fragment = mTabsPagerAdapter.getItem(position);


    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}