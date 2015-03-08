package com.cs48.lethe.ui.camera;

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

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.networking.HerokuRestClient;
import com.cs48.lethe.networking.PostPicture;
import com.cs48.lethe.ui.alertdialogs.NetworkUnavailableAlertDialog;
import com.cs48.lethe.utils.FileUtilities;
import com.cs48.lethe.utils.NetworkUtilities;
import com.cs48.lethe.utils.Picture;
import com.cs48.lethe.utils.PictureUtilities;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static android.hardware.Camera.CameraInfo;
import static android.view.View.OnClickListener;

/**
 * An activity that deals with working with the camera hardware,
 * taking pictures, and uploading to the server.
 */
public class CameraActivity extends ActionBarActivity {

    // Logcat tag
    public static final String TAG = CameraActivity.class.getSimpleName();

    // Initializations of UI elements
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

    // Instance variables
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private File mPictureFile;
    private boolean isCurrentlyPosting;
    private int mCurrentCameraId;
    private int mOrientation;

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

        // Sets the view to the camera layout
        setContentView(R.layout.activity_camera);

        // Injects the UI elements into the activity
        ButterKnife.inject(this);

        // Hides the action bar
        getSupportActionBar().hide();

        // Create an instance of Camera
        mCamera = getCameraInstance();

        try {

            // Forces the orientation of the current camera to match
            // the orientation of the activity (portrait)
            setCameraDisplayOrientation(mCurrentCameraId);

            // Create our Camera Preview view and set it as the content of our activity.
            mCameraPreview = new CameraPreview(this, mCamera);
            mFrameLayout.addView(mCameraPreview);

            // Shows the camera switch button if the devices has more than one camera
            showSupportedCameraSwitchButton();

            // Show flash button if phone supports flash
            showSupportedFlashButton();

            // Initially hides the post button, the progress bar, and the cancel button.
            mPostButton.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mCancelButton.setVisibility(View.GONE);

            // Front camera set to false because android defaults to back-facing camera
            isCurrentlyPosting = false;

            // Add a listeners to the all of the buttons
            mCaptureButton.setOnClickListener(new OnCaptureButtonClick());
            mBackButton.setOnClickListener(new OnBackButtonClick());
            mPostButton.setOnClickListener(new OnPostButtonClick());
            mCancelButton.setOnClickListener(new OnCancelButtonClick());
            mCameraSwitchButton.setOnClickListener(new OnCameraSwitchButtonClick());
            mFlashButton.setOnClickListener(new OnFlashButtonClick());
        }catch (NullPointerException e) {
            finish();
        }

    }

    /**
     * Called as part of the activity lifecycle when an activity
     * is going into the background, but has not (yet) been killed.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Release the camera resources so other apps are able to use the camera
        releaseCamera();
    }

    /**
     * Perform any final cleanup before an activity is destroyed. This can happen either
     * because the activity is finishing (someone called finish() on it, or because the
     * system is temporarily destroying this instance of the activity to save space.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Release the camera resources so other apps are able to use the camera
        releaseCamera();
    }

    /**
     * Called when the activity has detected the user's press of the back key.
     */
    @Override
    public void onBackPressed() {
         // Deletes the picture file if it exists and goes back to the main activity
         // (previous fragment) only if a picture is not currently being posted.
        if (!isCurrentlyPosting) {
            if (mPictureFile != null && mPictureFile.exists())
                mPictureFile.delete();
            super.onBackPressed();
        }
    }

    /**
     * Creates a new Camera object to access a particular hardware camera. If
     * the same camera is opened by other applications, this will throw a
     * RuntimeException.
     *
     * @return A new Camera object, connected, locked and ready for use.
     *         If the device does not have a back-facing camera, this
     *         returns null.
     */
    private Camera getCameraInstance() {
        Camera c = null;
        try {
            // Gets a camera instance of the back facing camera
            c = Camera.open();
            mCurrentCameraId = CameraInfo.CAMERA_FACING_BACK;
        } catch (Exception e) {
             // Camera is not available (in use or does not exist)
             // so go back to the main activity
            Log.d(TAG, "Camera is not available (in use or does not exist)");
            finish();
        }
        return c;
    }

    /**
     *  Shows the camera switch button only if the device has more than
     *  one camera. Otherwise, the camera switch button is hidden.
     */
    private void showSupportedCameraSwitchButton() {
        if (Camera.getNumberOfCameras() > 1)
            mCameraSwitchButton.setVisibility(View.VISIBLE);
        else
            mCameraSwitchButton.setVisibility(View.GONE);
    }

    /**
     *  Shows the camera flash button only if the device has flash
     *  capability. Otherwise, the camera flash button is hidden.
     */
    private void showSupportedFlashButton() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            mFlashButton.setVisibility(View.VISIBLE);
        } else
            mFlashButton.setVisibility(View.GONE);
    }

    /**
     * Sets the camera orientation to the orientation of the display
     * to keep it consistent. This is necessary because the app
     * only runs in portrait. This forces the camera to a
     * portrait orientation.
     *
     * Source code:
     * http://stackoverflow.com/questions/16128608/camera-preview-is-in-portrait-mode-but-image-captured-is-rotated
     *
     * @param cameraId The id of the camera
     */
    private void setCameraDisplayOrientation(int cameraId) {
        // Gets the information about the camera
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // Gets the rotation of the screen from its "natural" orientation.
        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        // Sets the angle based upon the rotation
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

        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            mOrientation = (info.orientation + degrees) % 360;
            mOrientation = (360 - mOrientation) % 360;  // compensate the mirror
        } else {  // back-facing
            mOrientation = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(mOrientation);
    }

    /**
     * Releases the camera for other applications by
     * disconnecting the Camera object resources.
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * TODO get this to work with text upload for lat/long (image upload works)
     *
     * NOT FUNCTIONING!!!
     *
     * This is to test working with the network library rather than using
     * the android async class.
     */
    private void disfunctionalPostPicture() {
//        try {
        String[] currentLocation = NetworkUtilities.getCurrentLocation(this);

        RequestParams params = new RequestParams();
//            params.put("avatar", mPictureFile, "image/jpeg");
        params.put("latitude", currentLocation[0]);
        params.put("longitude", currentLocation[1]);

        String url = getString(R.string.server) + getString(R.string.server_post);

        HerokuRestClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String jsonData = new String(responseBody);
                    JSONObject jsonObject = new JSONObject(jsonData);
                    DatabaseHelper databaseHelper = DatabaseHelper.getInstance(CameraActivity.this);
                    Picture picture = new Picture(jsonObject.getString(getString(R.string.json_id)),
                            jsonObject.getString(getString(R.string.json_date_posted)),
                            mPictureFile, mOrientation, 0, 0);
                    databaseHelper.insertPicture(picture);

                } catch (JSONException e) {
                    Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
                onPostPictureFailed();
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, "status code = " + statusCode);
                for (Header header : headers)
                    Log.d(TAG, "header = " + header);
                Log.d(TAG, "error = " + error.getLocalizedMessage());
                String response = new String(responseBody);
                Log.d(TAG, "response body = " + response);
            }
        });
    }

    /**
     * TESTING PURPOSES ONLY
     *
     * Adds the picture taken by the camera to the database.
     * This does not post to the server.
     */
    private void fakePostPicture() {
        onPostPictureStart();
        Date date = new Date();
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        Picture picture = new Picture(date.getTime() + "", new SimpleDateFormat("yyyyMMdd_HHmmss").
                format(date), mPictureFile, mOrientation, 0, 0);
        databaseHelper.insertPicture(picture);
        finish();
    }

    /**
     * Actions necessary before the picture is posted to the server.
     */
    public void onPostPictureStart() {
        // Sets the posting boolean to true
        isCurrentlyPosting = true;

        // Shows the progress bar to notify that the picture is being uploaded
        mProgressBar.setVisibility(View.VISIBLE);

        // Hides buttons that are necessary to hide during the upload
        mCaptureButton.setVisibility(View.INVISIBLE);
        mCancelButton.setVisibility(View.INVISIBLE);
        mPostButton.setVisibility(View.INVISIBLE);
        mBackButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Actions necessary after the picture fails to post to the server.
     */
    public void onPostPictureFailed() {
        // Sets the posting boolean to false
        isCurrentlyPosting = false;

        // Shows buttons that are necessary to show after a failed upload
        mCancelButton.setVisibility(View.VISIBLE);
        mPostButton.setVisibility(View.VISIBLE);

        // Hides the progress bar
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * Callback used to supply image data from a photo capture.
     */
    private class PictureTakenCallBack implements Camera.PictureCallback {
        /**
         * Called when image data is available after a picture is taken.
         * The format of the data depends on the context of the callback
         * and Camera.Parameters settings.
         *
         * @param data   a byte array of the picture data
         * @param camera the Camera service object
         */
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // Convert byte array to bitmap picture
            Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);

            // Get the dimensions of the picture
            int width = picture.getWidth();
            int height = picture.getHeight();

            // Get the proper scaling while keeping the aspect ratio to reduce size of picture
            double widthScale = (double) PictureUtilities.MAX_FULL_WIDTH / width;
            double heightScale = (double) PictureUtilities.MAX_FULL_HEIGHT / height;
            double scale = Math.min(widthScale, heightScale);
            width *= scale;
            height *= scale;

            // Resizes the picture with the new scaled dimensions
            Bitmap resizedPicture = Bitmap.createScaledBitmap(picture, width, height, true);

            // Creates a new file for the picture to be stored
            mPictureFile = FileUtilities.getOutputMediaFile(CameraActivity.this);

            // Writes the scaled bitmap picture to the file
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(mPictureFile);
                resizedPicture.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            mPostButton.setVisibility(View.VISIBLE);
            mCancelButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * A callback to be invoked when the capture button is clicked.
     */
    private class OnCaptureButtonClick implements OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Shows the cancel and post button

            //Below 2 lines are pasted in pictureTakenCallBAck
            //mPostButton.setVisibility(View.VISIBLE);
            //mCancelButton.setVisibility(View.VISIBLE);

            // Hides the capture, back, camera switch, and flash buttons
            mCaptureButton.setVisibility(View.GONE);
            mBackButton.setVisibility(View.GONE);
            mCameraSwitchButton.setVisibility(View.GONE);
            mFlashButton.setVisibility(View.GONE);

             // Triggers an asynchronous image capture. The camera
             // service will initiate a series of callbacks to the
             // application as the image capture progresses.
            mCamera.takePicture(null, null, new PictureTakenCallBack());
        }
    }

    /**
     * A callback to be invoked when the back button is clicked.
     */
    private class OnBackButtonClick implements OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Deletes the picture file if it exists and returns to the main activity
            if (mPictureFile != null && mPictureFile.exists())
                mPictureFile.delete();
            finish();
        }
    }

    /**
     * A callback to be invoked when the post button is clicked.
     */
    private class OnPostButtonClick implements OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
             // Posts the picture to the server if the network is available.
             // Otherwise, shows a network unavailable error dialog.
            if (NetworkUtilities.isNetworkAvailable(CameraActivity.this)) {
//                fakePostPicture();
//                disfunctionalPostPicture();
                new PostPicture(CameraActivity.this, mPictureFile, mOrientation).execute();
            } else {
                try {
                    new NetworkUnavailableAlertDialog().show(getFragmentManager(), TAG);
                }catch (IllegalStateException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * A callback to be invoked when the cancel button is clicked.
     */
    private class OnCancelButtonClick implements OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Shows the capture and back buttons
            mCaptureButton.setVisibility(View.VISIBLE);
            mBackButton.setVisibility(View.VISIBLE);

            // Hides the cancel and post buttons
            mCancelButton.setVisibility(View.INVISIBLE);
            mPostButton.setVisibility(View.INVISIBLE);

            // Shows the flash and camera switch buttons
            // if the device supports the functionality
            showSupportedFlashButton();
            showSupportedCameraSwitchButton();

            // Deletes the picture file if it exists
            if (mPictureFile != null && mPictureFile.exists())
                mPictureFile.delete();

            // Starts the camera preview again
            mCamera.startPreview();
        }
    }

    /**
     * A callback to be invoked when the camera switch button is clicked.
     */
    private class OnCameraSwitchButtonClick implements OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Sets the current camera ID to that of the inactive camera (the one being switched to)
            mCurrentCameraId = (mCurrentCameraId == CameraInfo.CAMERA_FACING_FRONT) ?
                    CameraInfo.CAMERA_FACING_BACK : CameraInfo.CAMERA_FACING_FRONT;

            // Releases current camera resources
            mCamera.release();

            // Opens the camera with the new camera ID
            mCamera = Camera.open(mCurrentCameraId);

            // Sets the orientation of the camera to that of the app
            setCameraDisplayOrientation(mCurrentCameraId);

            // Refreshes the camera preview to reflect the camera switch
            mCameraPreview.switchCamera(mCamera);
        }
    }

    /**
     * A callback to be invoked when the flash button is clicked.
     */
    private class OnFlashButtonClick implements OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            try {
                // Creates new parameter to set to the camera
                Camera.Parameters parameters = mCamera.getParameters();

                 // If the flash is currently off, then the flash turns on and the
                 // icon is changed to the flash on icon. Otherwise, the flash turns
                 // off and the icon is changed to the flash off icon.
                if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                    mFlashButton.setImageResource(R.drawable.ic_action_flash_on);
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                } else {
                    mFlashButton.setImageResource(R.drawable.ic_action_flash_off);
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }

                // Starts the camera preview with the new parameter
                mCamera.setParameters(parameters);
                mCamera.startPreview();

            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }
}
