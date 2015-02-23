package com.cs48.lethe.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.server.DislikePicture;
import com.cs48.lethe.server.LikePicture;
import com.cs48.lethe.ui.dialogs.OperationFailedDialog;
import com.cs48.lethe.utils.Image;
import com.cs48.lethe.utils.OnSwipeTouchListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * The activity that handles showing the full-sized image
 * whenever the user tabs on an image in the grid.
 */
public class FullPictureActivity extends ActionBarActivity {

    @InjectView(R.id.fullImageView)
    ImageView mImageView;
    @InjectView(R.id.deleteButton)
    ImageButton mDeleteButton;
    @InjectView(R.id.saveButton)
    ImageButton mCopyButton;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;

    public static final String TAG = FullPictureActivity.class.getSimpleName();
    public static final String VIEW_ONLY = "VIEW_ONLY";
    public static final String VIEW_OVERLAY = "VIEW_OVERLAY";

    public static final int FULL_IMAGE_REQUEST = 99;

    private Image mImage;

    /**
     * Hides the action bar and gets all of the necessary data from
     * the Bundle that was passed from the calling activity. Shows/hides
     * buttons on the screen depending upon which view state was requested.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_picture);

        ButterKnife.inject(this);

        // Hide action bar
        getSupportActionBar().hide();

        mProgressBar.setVisibility(View.GONE);

        // Get intent and extras
        Intent intent = getIntent();
        mImage = (Image) intent.getSerializableExtra("image");


        if (intent.getAction().equals(VIEW_OVERLAY)) {
            Picasso.with(this).load(mImage.getFile()).into(mImageView);
            showPictureOverlay();
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else {
            Picasso.with(this).load(mImage.getFullUrl()).into(mImageView);
            hidePictureOverlay();
            setUpGestureListener();
        }
    }

    public void setImageView(Image image) {
        mImage = image;

        if (!mImage.isHidden()) {
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mProgressBar.setVisibility(View.GONE);
                    mImageView.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    mProgressBar.setVisibility(View.GONE);
                    new OperationFailedDialog().show(getFragmentManager(), TAG);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };


            Picasso.with(this)
                    .load(mImage.getFullUrl())
                    .into(target);
        }
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
                new LikePicture(FullPictureActivity.this).execute(mImage.getId());
                finish();
            }

            /**
             * Swiping right hides the photo from the feed.
             */
            @Override
            public void onSwipeRight() {
                new DislikePicture(FullPictureActivity.this).execute(mImage.getId());
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
    }

    /**
     * Hides the like button but shows the delete and
     * copy buttons. Handles the visible button presses.
     */
    private void showPictureOverlay() {
        mDeleteButton.setVisibility(View.VISIBLE);
        mCopyButton.setVisibility(View.VISIBLE);

        /**
         * Deletes the image stored on the device and goes back to the grid.
         */
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FileUtilities.deleteImage(mImageUri);
                Toast.makeText(FullPictureActivity.this, "Deleted image", Toast.LENGTH_SHORT).show();
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
//                try {
//                    FileUtilities.saveImageForSharing(ImageActivity.this, mImageUri.getPath());
                Toast.makeText(FullPictureActivity.this, "Saved to shared storage.", Toast.LENGTH_SHORT).show();
//                } catch (IOException e) {
//                    Toast.makeText(ImageActivity.this, "Cannot copy to shared storage.", Toast.LENGTH_SHORT).show();
//                }
            }
        });
    }

}


