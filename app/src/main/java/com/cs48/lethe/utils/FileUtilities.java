package com.cs48.lethe.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

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

    // Logcat tag
    public static final String TAG = FileUtilities.class.getSimpleName();

    /**
     * Returns directory where pictures will be stored
     *
     * @param context Interface to global information about an application environment
     * @return Directory to store files.
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
            return context.getFilesDir();
        } else if (storageType.equals(StorageType.PRIVATE_EXTERNAL)) {
            /**
             This is like getFilesDir() in that these files will be deleted
             when the application is uninstalled. However, external files are
             not always available: they will disappear if the user mounts the
             external storage on a computer or removes it.
             */
            return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {
            /**
             This is where the user will typically place and manage their own files,
             so you should be careful about what you put here to ensure you don't erase
             their files or get in the way of their own organization.
             */
            return getSharedExternalDirectory(context);
        }
    }

    /**
     * Checks external storage is mounted.
     *
     * @return True if the SD card is mounted. False otherwise.
     */
    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }

    /**
     * Returns app subdirectory in the external public storage. If directory doesn't exist, then it's created.
     *
     * @param context Interface to global information about an application environment
     * @return Directory of shared public storage.
     */
    public static File getSharedExternalDirectory(Context context) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                context.getResources().getString(R.string.app_name).replace(" ", "").toLowerCase());
        if (!dir.mkdirs())
            Log.e(TAG, "Directory not created");
        return dir;
    }

    /**
     * Copies the image to the public storage directory and returns the Uri
     *
     * @param context Interface to global information about an application environment
     * @param picture The picture to save
     * @return True if successfully copied. False otherwise.
     * @throws IOException
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
     *
     * @param context Interface to global information about an application environment
     * @return The picture file
     */
    public static File getOutputMediaFile(Context context) {
        File mediaStorageDir = getFileDirectory(context);

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
    }

}
