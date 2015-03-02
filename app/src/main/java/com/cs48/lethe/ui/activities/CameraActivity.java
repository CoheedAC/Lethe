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

import com.cs48.lethe.R;
import com.cs48.lethe.database.DatabaseHelper;
import com.cs48.lethe.networking.HerokuRestClient;
import com.cs48.lethe.networking.PostPicture;
import com.cs48.lethe.ui.dialogs.NetworkUnavailableDialog;
import com.cs48.lethe.ui.view_helpers.CameraPreview;
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
    private boolean isCurrentlyPosting;
    private int mCurrentCameraId;

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
        setContentView(R.layout.activity_camera);

        ButterKnife.inject(this);

        // Hides the action bar
        getSupportActionBar().hide();

        // Create an instance of Camera
        mCamera = getCameraInstance();

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

        // Initially hides the post button, the progress bar,
        // and the cancel button.
        mPostButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);

        // Front camera set to false because android defaults to back-facing camera
        isCurrentlyPosting = false;

        // Add a listeners to the all of the button
        mCaptureButton.setOnClickListener(new OnCaptureButtonClick());
        mBackButton.setOnClickListener(new OnBackButtonClick());
        mPostButton.setOnClickListener(new OnPostButtonClick());
        mCancelButton.setOnClickListener(new OnCancelButtonClick());
        mCameraSwitchButton.setOnClickListener(new OnSwitchCameraButtonClick());
        mFlashButton.setOnClickListener(new OnFlashButtonClick());
    }

    /**
     * Called as part of the activity lifecycle when an activity
     * is going into the background, but has not (yet) been killed.
     */
    @Override
    protected void onPause() {
        super.onPause();
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
        releaseCamera();
    }

    /**
     * Called when the activity has detected the user's press of the back key.
     */
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
            mCurrentCameraId = CameraInfo.CAMERA_FACING_BACK;
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.d(TAG, "Camera is not available (in use or does not exist)");
            mCurrentCameraId = -1;
            finish();
        }
        return c; // returns null if camera is unavailable
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

    private void setCameraDisplayOrientation(int cameraId) {
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

    private void postPicture() {
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
                            mPictureFile, 0, 0);
                    databaseHelper.insertPicture(picture);

                } catch (JSONException e) {
                    Log.e(TAG, e.getClass().getName() + ": " + e.getLocalizedMessage());
                }
                onPostPictureEnd();
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
     * Adds the picture taken by the camera to the database.
     * This does not post to the server.
     * TESTING PURPOSES ONLY
     */
    private void fakePostPicture() {
        onPostPictureStart();
        Date date = new Date();
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        Picture picture = new Picture(date.getTime() + "", new SimpleDateFormat("yyyyMMdd_HHmmss").
                format(date), mPictureFile, 0, 0);
        databaseHelper.insertPicture(picture);
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

    /**
     * Callback used to supply image data from a photo capture.
     */
    private class PictureTakenCallBack implements Camera.PictureCallback {
        /**
         * Called when image data is available after a picture is taken.
         * The format of the data depends on the context of the callback
         * and Camera.Parameters settings.
         *
         * @param data a byte array of the picture data
         * @param camera the Camera service object
         */
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

    /**
     * A callback to be invoked when a view is clicked.
     */
    private class OnCaptureButtonClick implements OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
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

    /**
     * A callback to be invoked when a view is clicked.
     */
    private class OnBackButtonClick implements OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            if (mPictureFile != null && mPictureFile.exists())
                mPictureFile.delete();
            finish();
        }
    }

    /**
     * A callback to be invoked when a view is clicked.
     */
    private class OnPostButtonClick implements OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            if (NetworkUtilities.isNetworkAvailable(CameraActivity.this)) {
//                fakePostPicture();
//                postPicture();
                new PostPicture(CameraActivity.this, mPictureFile).execute();
            } else {
                try {
                    new NetworkUnavailableDialog().show(getFragmentManager(), TAG);
                }catch (IllegalStateException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * A callback to be invoked when a view is clicked.
     */
    private class OnCancelButtonClick implements OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
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

    /**
     * A callback to be invoked when a view is clicked.
     */
    private class OnSwitchCameraButtonClick implements OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            mCurrentCameraId = (mCurrentCameraId == CameraInfo.CAMERA_FACING_FRONT) ?
                    CameraInfo.CAMERA_FACING_BACK : CameraInfo.CAMERA_FACING_FRONT;
            mCamera.release();
            mCamera = Camera.open(mCurrentCameraId);
            setCameraDisplayOrientation(mCurrentCameraId);
            mCameraPreview.refreshCamera(mCamera);
        }
    }

    /**
     * A callback to be invoked when a view is clicked.
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
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                    mFlashButton.setImageResource(R.drawable.ic_action_flash_on);
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                } else {
                    mFlashButton.setImageResource(R.drawable.ic_action_flash_off);
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (Exception e) {
                FileUtilities.logResults(CameraActivity.this, TAG, "Flash button not functional");
            }
        }
    }
}
