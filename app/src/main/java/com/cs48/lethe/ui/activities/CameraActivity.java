package com.cs48.lethe.ui.activities;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.networking.PostPicture;
import com.cs48.lethe.ui.dialogs.NetworkUnavailableDialog;
import com.cs48.lethe.ui.view_helpers.CameraPreview;
import com.cs48.lethe.utils.ActionCodes;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.NetworkUtilities;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.PictureUtilities;

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
    @InjectView(R.id.cameraSwitchButton)
    ImageButton mCameraSwitchButton;
    @InjectView(R.id.flashButton)
    ImageButton mFlashButton;

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private File mPictureFile;
    private boolean isFrontCameraOn;
    private boolean isCurrentlyPosting;
    private int mCurrentCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ButterKnife.inject(this);

        try {
            // Create an instance of Camera
            mCamera = Camera.open();
            mCurrentCamera = 0;

            /**
             * COMMENT THIS LINE IF YOU ARE HAVING ORIENTATION ISSUES
             * AS WELL AS THE SAME METHOD CALLS IN THE switchCamera()
             * METHOD
             */
            setCameraDisplayOrientation(mCurrentCamera);

            // Hides the action bar
            getSupportActionBar().hide();

            // Create our Camera Preview view and set it as the content of our activity.
            mCameraPreview = new CameraPreview(this, mCamera);
            mFrameLayout.addView(mCameraPreview);

            // Shows the camera switch button if the devices has more than one camera
            showSupportedCameraSwitchButton();

            // Show flash button if phone supports flash
            showSupportedFlashButton();

            // Initially hides the post button, the progress bar,
            // and the cancel button.
            mPostButton.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mCancelButton.setVisibility(View.GONE);

            // front camera to false because android
            // defaults to back camera
            isFrontCameraOn = false;
            isCurrentlyPosting = false;

            // Add a listeners to the all of the button
            mCaptureButton.setOnClickListener(new CaptureButtonOnClickListener());
            mBackButton.setOnClickListener(new BackButtonOnClickListener());
            mPostButton.setOnClickListener(new PostButtonOnClickListener());
            mCancelButton.setOnClickListener(new CancelButtonOnClickListener());
            mCameraSwitchButton.setOnClickListener(new SwitchCameraButtonOnClickListener());
            mFlashButton.setOnClickListener(new FlashButtonOnClickListener());
        }catch (Exception e) {
            Toast.makeText(this,"Camera not working. Trying retarting emulator AND enable camera before oping app", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * release the camera immediately on pause event
     */
    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    @Override
    public void onBackPressed() {
        if (!isCurrentlyPosting) {
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
        isCurrentlyPosting = true;
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
        isCurrentlyPosting = false;
        mCancelButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mPostButton.setVisibility(View.VISIBLE);
    }

    private void showSupportedCameraSwitchButton() {
        if (Camera.getNumberOfCameras() > 1)
            mCameraSwitchButton.setVisibility(View.VISIBLE);
        else
            mCameraSwitchButton.setVisibility(View.GONE);
    }

    private void showSupportedFlashButton() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            mFlashButton.setVisibility(View.VISIBLE);
        } else
            mFlashButton.setVisibility(View.GONE);
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
        if (isFrontCameraOn) {
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
                isFrontCameraOn = true;
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
                isFrontCameraOn = false;
                break;
            }
        }
        return cameraId;
    }

    class PictureTakenCallBack implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
            int width = picture.getWidth();
            int height = picture.getHeight();

            double widthScale = (double) PictureUtilities.MAX_FULL_WIDTH / width;
            double heightScale = (double) PictureUtilities.MAX_FULL_HEIGHT / height;
            double scale = Math.min(widthScale, heightScale);
            width *= scale;
            height *= scale;

            Bitmap resizedPicture = Bitmap.createScaledBitmap(picture, width, height, true);
            mPictureFile = FileUtilities.getOutputMediaFile(CameraActivity.this);
            try {
                FileOutputStream fos = new FileOutputStream(mPictureFile);
                resizedPicture.compress(Bitmap.CompressFormat.JPEG, 100, fos);
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
            mCancelButton.setVisibility(View.VISIBLE);
            mCaptureButton.setVisibility(View.GONE);
            mBackButton.setVisibility(View.GONE);
            mCameraSwitchButton.setVisibility(View.GONE);
            mFlashButton.setVisibility(View.GONE);
            mCamera.takePicture(null, null, new PictureTakenCallBack());
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
        }
    }

    class CancelButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mCancelButton.setVisibility(View.INVISIBLE);
            mPostButton.setVisibility(View.INVISIBLE);
            mCaptureButton.setVisibility(View.VISIBLE);
            mBackButton.setVisibility(View.VISIBLE);
            showSupportedFlashButton();
            showSupportedCameraSwitchButton();
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

    class FlashButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                    mFlashButton.setImageResource(R.drawable.ic_action_flash_on);
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(parameters);
                    mCamera.startPreview();
                } else if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                    mFlashButton.setImageResource(R.drawable.ic_action_flash_off);
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                    mCamera.startPreview();
                }
            }catch (Exception e) {
                FileUtilities.logResults(CameraActivity.this, TAG, "Flash button not functional");
            }
        }
    }

}
