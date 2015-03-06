package com.cs48.lethe.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.database.ApplicationSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class that handles tasks related to file storage
 * and manipulation.
 */
public class FileUtilities {

    public static final String LOG_TAG = FileUtilities.class.getSimpleName();

    /**
     * Returns directory where images will be stored
     */
    public static File getFileDirectory(Context context) {
        ApplicationSettings settings = new ApplicationSettings(context);
        String storageType = settings.getStoragePreference();
        if (storageType.equals(StorageType.INTERNAL) || !isExternalStorageAvailable()) {
            /**
             Returns the absolute path to the directory on the filesystem
             where files created with openFileOutput(String, int) are stored.
             In other words, this is internal storage.
             */
            Log.d(LOG_TAG, "INTERNAL");
            return context.getFilesDir();
        } else if (storageType.equals(StorageType.PRIVATE_EXTERNAL)) {
            /**
             This is like getFilesDir() in that these files will be deleted
             when the application is uninstalled. However, external files are
             not always available: they will disappear if the user mounts the
             external storage on a computer or removes it.
             */
            Log.d(LOG_TAG, "PRIVATE EXTERNAL");
            return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {
            /**
             This is where the user will typically place and manage their own files,
             so you should be careful about what you put here to ensure you don't erase
             their files or get in the way of their own organization.
             */
            Log.d(LOG_TAG, "PUBLIC EXTERNAL");
            return getSharedExternalDirectory(context);
        }
    }

    /**
     * Prints out a message for debugging
     */
    public static void logResults(Context context, String classTag, String messageResult) {
        Toast.makeText(context, messageResult, Toast.LENGTH_SHORT).show();
        Log.d(classTag, messageResult);
    }

    /**
     * Returns true if external storage is mounted. False otherwise.
     */
    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }

    /**
     * Returns app subdirectory in the external public storage. If directory doesn't exist, then it's created.
     */
    public static File getSharedExternalDirectory(Context context) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                context.getResources().getString(R.string.app_name).replace(" ", "").toLowerCase());
        if (!dir.mkdirs())
            Log.e(LOG_TAG, "Directory not created");
        return dir;
    }

    /**
     * Copies the image to the public storage directory and returns the Uri
     */
    public static boolean savePictureForSharing(Context context, Picture picture) throws IOException {
        File destination = new File(getSharedExternalDirectory(context) + File.separator + picture.getFile().getName());

        if (!destination.exists()) {
            FileInputStream fileInputStream = new FileInputStream(picture.getFile());
            FileOutputStream fileOutputStream = new FileOutputStream(destination);
            FileChannel inChannel = fileInputStream.getChannel();
            FileChannel outChannel = fileOutputStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            fileInputStream.close();
            fileOutputStream.close();
            return true;
        }
        return false;
    }

    /**
     * Create a File for saving an image or video
     */
    public static File getOutputMediaFile(Context context) {
        File mediaStorageDir = getFileDirectory(context);

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

}
