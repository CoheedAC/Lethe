package com.cs48.lethe.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.cs48.lethe.R;
import com.cs48.lethe.ui.adapters.TabsPagerAdapter;

/**
 * The main activity where the app launches. It handles all of the tab fragments
 * and action bar button presses.
 */
public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private TabsPagerAdapter mTabsPagerAdapter;
    private ViewPager mViewPager;

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
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the four
        // primary sections of the activity.
        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager(), this);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Show app icon
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.ic_launcher);

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
     * Initialize the contents of the Activity's standard options menu.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_clear_cache).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        return true;
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item The menu item that was selected.
     *
     * @return Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Starts camera activity if camera button pressed
        if (id == R.id.action_camera) {
            startActivity(new Intent(this, CameraActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a tab enters the selected state.
     *
     * @param tab The tab that was selected
     * @param fragmentTransaction A FragmentTransaction for queuing fragment operations
     *                            to execute during a tab switch. The previous tab's unselect
     *                            and this tab's select will be executed in a single transaction.
     *                            This FragmentTransaction does not support being added to the back stack.
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
                setTitle(R.string.title_tab1);
                break;
            case 1:
                setTitle(R.string.title_tab2);
                break;
            case 2:
                setTitle(R.string.title_tab3);
                break;
            case 3:
                setTitle(R.string.title_tab4);
                break;
        }
    }

    /**
     * Called when a tab exits the selected state.
     *
     * @param tab The tab that was unselected
     * @param fragmentTransaction A FragmentTransaction for queuing fragment operations to execute
     *                            during a tab switch. This tab's unselect and the newly selected
     *                            tab's select will be executed in a single transaction. This
     *                            FragmentTransaction does not support being added to the back stack.
     */
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * Called when a tab that is already selected is chosen again by the user.
     *
     * @param tab The tab that was reselected.
     * @param fragmentTransaction A FragmentTransaction for queuing fragment operations
     *                            to execute once this method returns. This FragmentTransaction
     *                            does not support being added to the back stack.
     */
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

}