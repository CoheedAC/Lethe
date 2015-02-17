package com.cs48.lethe.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.server.DownloadFullImage;
import com.cs48.lethe.server.LikeImage;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.OnSwipeTouchListener;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * The activity that handles showing the full-sized image
 * whenever the user tabs on an image in the grid.
 */
public class FullScreenImageActivity extends ActionBarActivity {

    @InjectView(R.id.fullImageView)
    ImageView mImageView;
    @InjectView(R.id.deleteButton)
    ImageButton mDeleteButton;
    @InjectView(R.id.saveButton)
    ImageButton mCopyButton;
    @InjectView(R.id.likeButton)
    ImageButton mLikeButton;

    public static final String VIEW_ONLY = "VIEW_ONLY";
    public static final String VIEW_OVERLAY = "VIEW_OVERLAY";

    public static final int FULL_IMAGE_REQUEST = 99;

    private Uri mImageUri;
    private int mImagePosition;
    private String mUniqueId;

    /**
     * Hides the action bar and gets all of the necessary data from
     * the Bundle that was passed from the calling activity. Shows/hides
     * buttons on the screen depending upon which view state was requested.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        ButterKnife.inject(this);

        // Hide action bar
        getSupportActionBar().hide();

        // Get intent and extras
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mImageUri = intent.getData();
            mImagePosition = extras.getInt("position");

            //mImageView.setImageURI(mImageUri);
            mImageView.setImageBitmap(FileUtilities.getValidSizedBitmap(this.getContentResolver(), mImageUri));
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            mUniqueId = FileUtilities.getFileName(mImageUri.getPath());
            mUniqueId = FileUtilities.getUniqueId(mUniqueId);

            if (intent.getAction().equals(VIEW_OVERLAY)) {
                showPictureOverlay();
            } else {
                hidePictureOverlay();
                downloadFullImage();
                setUpGestureListener();
            }
        }
    }

    /**
     * Downloads the full-sized image from the server.
     */
    private void downloadFullImage() {
        String fullImageRequest = "https://frozen-sea-8879.herokuapp.com/" + mUniqueId;
        new DownloadFullImage(this, mImageUri, mImageView).execute(fullImageRequest);
    }

    /**
     * Handles the swipe / tap gestures.
     */
    private void setUpGestureListener() {
        mImageView.setOnTouchListener(new OnSwipeTouchListener(this) {

            /**
             * Swiping left likes the photo then goes back to the feed.
             */
            @Override
            public void onSwipeLeft() {
                String likeImageRequest = "https://frozen-sea-8879.herokuapp.com/like/" + mUniqueId;
                new LikeImage(FullScreenImageActivity.this).execute(likeImageRequest);
                finish();
            }

            /**
             * UNIMPLEMENTED
             * Swiping right hides the photo from the feed.
             */
            @Override
            public void onSwipeRight() {
                Toast.makeText(FullScreenImageActivity.this, "Photo hidden", Toast.LENGTH_SHORT).show();
                finish();
            }

            /**
             * Tapping anywhere on the screen goes back to the feed.
             */
            @Override
            public void onSingleTap() {
                finish();
            }
        });
    }

    /**
     * Hides the delete and copy button but shows the
     * like button. Also handles like button presses.
     */
    private void hidePictureOverlay() {
        mDeleteButton.setVisibility(View.GONE);
        mCopyButton.setVisibility(View.GONE);
        mLikeButton.setVisibility(View.VISIBLE);

        mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String likeImageRequest = "https://frozen-sea-8879.herokuapp.com/like/" + mUniqueId;
                new LikeImage(FullScreenImageActivity.this).execute(likeImageRequest);
            }
        });
    }

    /**
     * Hides the like button but shows the delete and
     * copy buttons. Handles the visible button presses.
     */
    private void showPictureOverlay() {
        mDeleteButton.setVisibility(View.VISIBLE);
        mCopyButton.setVisibility(View.VISIBLE);
        mLikeButton.setVisibility(View.GONE);


        /**
         * Deletes the image stored on the device and goes back to the grid.
         */
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileUtilities.deleteImage(mImageUri);
                Toast.makeText(FullScreenImageActivity.this, "Deleted image #" + (mImagePosition + 1), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        /**
         * Copies the image from the private external (or internal) storage
         * into the public storage where othe apps can access the photo.
         */
        mCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FileUtilities.saveImageForSharing(FullScreenImageActivity.this, mImageUri.getPath());
                    Toast.makeText(FullScreenImageActivity.this, "Saved to shared storage.", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(FullScreenImageActivity.this, "Cannot copy to shared storage.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}


