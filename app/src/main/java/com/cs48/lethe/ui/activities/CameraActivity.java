package com.cs48.lethe.ui.activities;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.networking.PostPicture;
import com.cs48.lethe.ui.dialogs.NetworkUnavailableDialog;
import com.cs48.lethe.ui.view_helpers.CameraPreview;
import com.cs48.lethe.utils.ActionCodes;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.NetworkUtilities;
import com.cs48.lethe.utils.Picture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static android.hardware.Camera.CameraInfo;

public class CameraActivity extends ActionBarActivity {

    public static final String TAG = CameraActivity.class.getSimpleName();

    @InjectView(R.id.cameraPreview)
    FrameLayout mFrameLayout;
    @InjectView(R.id.captureButton)
    ImageButton mCaptureButton;
    @InjectView(R.id.postButton)
    ImageButton mPostButton;
    @InjectView(R.id.backButton)
    ImageButton mBackButton;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.cancelButton)
    ImageButton mCancelButton;
    @InjectView(R.id.switchCameraImageButton)
    ImageButton mSwitchCameraButton;

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private File mPictureFile;
    private boolean cameraFront;
    private boolean mCurrentlyPosting;
    private int mCurrentCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ButterKnife.inject(this);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        /**
         * COMMENT THIS LINE IF YOU ARE HAVING ORIENTATION ISSUES
         * AS WELL AS THE SAME METHOD CALL IN THE switchCamera()
         * METHOD
         */
        setCameraDisplayOrientation(mCurrentCamera);

        // Hides the action bar
        getSupportActionBar().hide();

        // Create our Preview view and set it as the content of our activity.
        mCameraPreview = new CameraPreview(this, mCamera);
        mFrameLayout.addView(mCameraPreview);

        if (Camera.getNumberOfCameras() > 1)
            mSwitchCameraButton.setVisibility(View.VISIBLE);
        else
            mSwitchCameraButton.setVisibility(View.GONE);

        cameraFront = false;
        mCurrentlyPosting = false;
        mPostButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);

        // Add a listener to the Capture button
        mCaptureButton.setOnClickListener(new CaptureButtonOnClickListener());
        mBackButton.setOnClickListener(new BackButtonOnClickListener());
        mPostButton.setOnClickListener(new PostButtonOnClickListener());
        mCancelButton.setOnClickListener(new CancelButtonOnClickListener());
        mSwitchCameraButton.setOnClickListener(new SwitchCameraButtonOnClickListener());
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    @Override
    public void onBackPressed() {
        if (!mCurrentlyPosting) {
            if (mPictureFile != null && mPictureFile.exists())
                mPictureFile.delete();
            super.onBackPressed();
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            mCurrentCamera = 0;
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.d(TAG, "Camera is not available (in use or does not exist)");
            mCurrentCamera = -1;
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Adds the picture taken by the camera to the database.
     * This does not post to the server.
     * TESTING PURPOSES ONLY
     */
    private void fakePostPicture() {
        onPostPictureStart();
        Date date = new Date();
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        databaseHelper.insertPictureToMeTable(
                new Picture(date.getTime() + "", new SimpleDateFormat("yyyyMMdd_HHmmss").
                        format(date), mPictureFile, 0, 0));
        setResult(ActionCodes.POST_SUCCESS);
        finish();
    }

    /**
     * Hides the back button in the action bar. Hides the
     * post button. And changes the title to reflect
     * that the image is currently being posted
     * to the server.
     */
    public void onPostPictureStart() {
        mCurrentlyPosting = true;
        mCancelButton.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mPostButton.setVisibility(View.INVISIBLE);
        mCaptureButton.setVisibility(View.INVISIBLE);
        mBackButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Shows the back button in the action bar. Shows the
     * post button. And changes the title back to the
     * normal title.
     */
    public void onPostPictureEnd() {
        mCurrentlyPosting = false;
        mCancelButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mPostButton.setVisibility(View.VISIBLE);
        mCaptureButton.setVisibility(View.VISIBLE);
        mBackButton.setVisibility(View.VISIBLE);
    }

    public void setCameraDisplayOrientation(int cameraId) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    public void switchCamera() {
        //if the camera preview is the front
        int cameraId;
        if (cameraFront) {
            cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview
                mCamera.release();
                mCamera = Camera.open(cameraId);
                setCameraDisplayOrientation(cameraId);
                mCameraPreview.refreshCamera(mCamera);
            }
        } else {
            cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera.release();
                mCamera = Camera.open(cameraId);
                setCameraDisplayOrientation(cameraId);
                mCameraPreview.refreshCamera(mCamera);
            }
        }
        setCameraDisplayOrientation(cameraId);
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    class PictureCallBack implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mPictureFile = FileUtilities.getOutputMediaFile(CameraActivity.this);

            try {
                FileOutputStream fos = new FileOutputStream(mPictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    }

    class CaptureButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // get an image from the camera
            mPostButton.setVisibility(View.VISIBLE);
            mCaptureButton.setVisibility(View.GONE);
            mCancelButton.setVisibility(View.VISIBLE);
            mBackButton.setVisibility(View.GONE);
            mSwitchCameraButton.setVisibility(View.GONE);
            mCamera.takePicture(null, null, new PictureCallBack());
        }
    }

    class BackButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mPictureFile != null && mPictureFile.exists())
                mPictureFile.delete();
            setResult(ActionCodes.CAMERA_CAPTURE_CANCELLED);
            finish();
        }
    }

    class PostButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (NetworkUtilities.isNetworkAvailable(CameraActivity.this)) {
//                fakePostPicture();
                    new PostPicture(CameraActivity.this, mPictureFile).execute();
            } else {
                new NetworkUnavailableDialog().show(getFragmentManager(), TAG);
            }

            finish();
        }
    }

    class CancelButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mCancelButton.setVisibility(View.INVISIBLE);
            mPostButton.setVisibility(View.INVISIBLE);
            mCaptureButton.setVisibility(View.VISIBLE);
            mBackButton.setVisibility(View.VISIBLE);
            mSwitchCameraButton.setVisibility(View.VISIBLE);
            if (mPictureFile != null && mPictureFile.exists())
                mPictureFile.delete();
            mCamera.startPreview();
        }
    }

    class SwitchCameraButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switchCamera();
        }
    }

}
