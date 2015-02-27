package com.cs48.lethe.ui.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.cs48.lethe.utils.ActionCodes;
import com.cs48.lethe.utils.FileUtilities;

import java.io.File;

/**
 * The main activity where the app launches. It handles all of the tab fragments
 * and action bar button presses.
 */
public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private TabsPagerAdapter mTabsPagerAdapter;
    private ViewPager mViewPager;
    private Uri mImageUri;

    /**
     * Creates the action bar and title.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the four
        // primary sections of the activity.
        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager(), this);

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

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTabsPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);

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
        menu.findItem(R.id.action_clear_cache).setVisible(false);
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
            startCamera();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Starts built-in camera functionality and sets path to store file
     */
    private void startCamera() {
        try {
            // create Intent to take a picture and return control to the calling application
            Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File imageFile = FileUtilities.savePostedImage(this); // create a file to save the image
            mImageUri = Uri.fromFile(imageFile);
            imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri); // set the image file name

            // start the image capture Intent
            startActivityForResult(imageCaptureIntent, ActionCodes.CAMERA_CAPTURE_REQUEST);
        } catch (ActivityNotFoundException e) {
            FileUtilities.logResults(this, LOG_TAG, "Whoops - your device doesn't support capturing images!");
        }
    }

    /**
     * Handles responses when activities are done / destroyed
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Camera activity result
        if (requestCode == ActionCodes.CAMERA_CAPTURE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Intent postImageIntent = new Intent(this, PostPictureActivity.class);
                if (data != null && data.getData() != null)
                    postImageIntent.setData(data.getData());
                else
                    postImageIntent.setData(mImageUri);
                startActivityForResult(postImageIntent, ActionCodes.POST_PICTURE_REQUEST);
            } else if (resultCode == RESULT_CANCELED) {
                File imageFile;
                if (data != null && data.getData() != null)
                    imageFile = new File(data.getData().getPath());
                else
                    imageFile = new File(mImageUri.getPath());
                imageFile.delete();
            }
        }

        // PostPicture activity result
        if (requestCode == ActionCodes.POST_PICTURE_REQUEST) {
            if (resultCode == ActionCodes.POST_SUCCESS) {
                FeedFragment feedFragment = (FeedFragment) findFragmentByPosition(0);
                if (feedFragment != null)
                    feedFragment.fetchFeedFromServer();

                MeFragment meFragment = (MeFragment) findFragmentByPosition(2);
                if (meFragment != null)
                    meFragment.fetchMePicturesFromDatabase();
            } else if (resultCode == ActionCodes.POST_CANCELLED) {
                startCamera();
            }
        }
    }

    /**
     * Returns the fragment at the given position
     */
    private Fragment findFragmentByPosition(int position) {
        return getSupportFragmentManager()
                .findFragmentByTag(TabsPagerAdapter.getFragmentTag(mViewPager.getId(), position));
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

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


}