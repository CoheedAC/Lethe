package com.cs48.lethe.ui.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.utils.FileUtilities;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PictureActivity extends ActionBarActivity {

    @InjectView(R.id.fullImageView)
    ImageView mImageView;
    @InjectView(R.id.deleteButton)
    ImageButton mDeleteButton;
    @InjectView(R.id.saveButton)
    ImageButton mCopyButton;

    private boolean viewOnly;
    private Uri mImageUri;
    private int mImagePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        ButterKnife.inject(this);

        getSupportActionBar().hide();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            viewOnly = extras.getBoolean("view_only");
            mImageUri = Uri.parse(extras.getString("uri"));
            mImagePosition = extras.getInt("position");

            mImageView.setImageURI(mImageUri);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            if (!viewOnly) {
                showPictureOverlay();
            } else {
                mDeleteButton.setVisibility(View.GONE);
                mCopyButton.setVisibility(View.GONE);
            }
        }
    }

    private void showPictureOverlay() {
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileUtilities.deleteImage(PictureActivity.this, mImageUri);
                Toast.makeText(PictureActivity.this, "Deleted image #" + (mImagePosition + 1), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        mCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FileUtilities.saveImageForSharing(PictureActivity.this, mImageUri.getPath());
                    Toast.makeText(PictureActivity.this, "Saved to shared storage.", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(PictureActivity.this, "Cannot copy to shared storage.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

}


