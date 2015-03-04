package com.cs48.lethe.ui.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * An extension of SurfaceView that handles the creation,
 * change, and destruction of the view based upon the camera.
 */
@SuppressLint("ViewConstructor")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    // Logcat tag
    public static final String TAG = CameraPreview.class.getSimpleName();

    // Instance variables
    private SurfaceHolder mHolder;
    private Camera mCamera;

    /**
     * The constructor that creates the Camera Preview
     *
     * @param context Interface to global information about an application environment
     * @param camera  The camera currently in-use
     */
    public CameraPreview(Context context, Camera camera) {
        super(context);

        // Sets the camera for the SurfaceView
        mCamera = camera;

        // Gets the SurfaceHolder which providing access and control
        // over this SurfaceView's underlying surface and adds a
        // SurfaceView callback interface to it.
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    /**
     * This is called immediately after the surface is first created.
     *
     * @param holder The SurfaceHolder whose surface is being created.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Sets the camera to display the preview with the
        // SurfaceHolder and starts the camera preview
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    /**
     * This is called immediately before a surface is being destroyed.
     *
     * @param holder The SurfaceHolder whose surface is being destroyed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Release the camera resources so other apps are able to use the camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * This is called immediately after any structural changes (format or size)
     * have been made to the surface. This method is always called at least once,
     * after surfaceCreated(SurfaceHolder).
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new PixelFormat of the surface.
     * @param w      The new width of the surface.
     * @param h      The new height of the surface.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }

    /**
     * Switches the camera preview from the current camera
     * in use to the desired camera.
     *
     * @param desiredCamera The camera that will be switched to
     */
    public void switchCamera(Camera desiredCamera) {
        if (mHolder.getSurface() == null) {
            // Preview surface does not exist
            return;
        }
        // Stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // Set the current camera to the new camera
        mCamera = desiredCamera;

        // Start preview with the new camera
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

}