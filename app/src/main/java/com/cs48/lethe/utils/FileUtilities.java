package com.cs48.lethe.utils;

<<<<<<< HEAD
=======
import android.content.Context;
import android.graphics.Bitmap;
>>>>>>> origin/mergeTestBranch
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

<<<<<<< HEAD
import java.io.File;
=======
import com.cs48.lethe.ApplicationSettings;
import com.cs48.lethe.R;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
>>>>>>> origin/mergeTestBranch
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by maxkohne on 1/27/15.
 */
<<<<<<< HEAD
public class FileUtilities{

    public static final String TAG = FileUtilities.class.getSimpleName();

    /*
    public static void saveAssetImage(Context context, String assetName) {
        File fileDirectory = getFileDirectory(context);
        File fileToWrite = new File(fileDirectory, assetName);

        AssetManager assetManager = context.getAssets();
        try {
            InputStream in = assetManager.open(assetName);
            FileOutputStream out = new FileOutputStream(fileToWrite);
            copyFile(in, out);
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getFileDirectory (Context context) {
        MemeMakerApplicationSettings settings = new MemeMakerApplicationSettings(context);
        String storageType = settings.getStoragePreference();
        if (storageType.equals(StorageType.INTERNAL)) {
            return context.getFilesDir();
        }else {
            if (isExternalStorageAvailable()) {
                if (storageType.equals(StorageType.PRIVATE_EXTERNAL)) {
                    return context.getExternalFilesDir(null);
                }
                else {
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                }
            }else {
=======
public class FileUtilities {

    public static final String TAG = FileUtilities.class.getSimpleName();


    /**
     * Returns directory where images will be stored
     */
    public static File getFileDirectory(Context context) {
        ApplicationSettings settings = new ApplicationSettings(context);
        String storageType = settings.getStoragePreference();
        String subdirectory = getSubdirectoryName(context);

        if (storageType.equals(StorageType.INTERNAL)) {
            /**
            Returns the absolute path to the directory on the filesystem
            where files created with openFileOutput(String, int) are stored.
            In other words, this is internal storage.
             */
            return context.getFilesDir();

        } else {
            if (isExternalStorageAvailable()) {
                if (storageType.equals(StorageType.PRIVATE_EXTERNAL)) {
                    /**
                    This is like getFilesDir() in that these files will be deleted
                    when the application is uninstalled. However, external files are
                    not always available: they will disappear if the user mounts the
                    external storage on a computer or removes it.
                     */
                    return context.getExternalFilesDir(subdirectory);

                } else {
                    /**
                    This is where the user will typically place and manage their own files,
                    so you should be careful about what you put here to ensure you don't erase
                    their files or get in the way of their own organization.
                     */
                    return getExternalStoragePublicDirectory(subdirectory);
                }
            } else {
                // Internal storage
>>>>>>> origin/mergeTestBranch
                return context.getFilesDir();
            }
        }
    }

<<<<<<< HEAD
=======
    /**
     Returns true if external storage is mounted. False otherwise.
     */
>>>>>>> origin/mergeTestBranch
    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state)) ? true : false;
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

<<<<<<< HEAD
    public static File[] listFiles(Context context) {
        File fileDirectory = getFileDirectory(context);
        File [] filteredFiles = fileDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.getAbsolutePath().contains(".jpg")) {
                    return true;
                }else
                    return false;
=======
    /**
     Returns an array of jpg files that are saved in the storage directory
     */
    public static File[] listFiles(Context context) {
        File fileDirectory = getFileDirectory(context);
        File[] filteredFiles = fileDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.getAbsolutePath().contains(".jpg")) ? true : false;
>>>>>>> origin/mergeTestBranch
            }
        });
        return filteredFiles;
    }

<<<<<<< HEAD
    public static Uri saveImageForSharing(Context context, Bitmap bitmap,  String assetName) {
        File fileToWrite = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), assetName);
=======
    /**
    Returns app subdirectory in the external public storage. If directory doesn't exist, then it's created.
    */
    public static File getExternalStoragePublicDirectory(String subdirectory) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), subdirectory);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.d(TAG, "Failed to make directory.");
            }
        }
        return dir;
    }

    /**
     Returns string of app name
     */
    private static String getSubdirectoryName(Context context) {
        return context.getResources().getString(R.string.app_name).replace(" ", "");
    }

    /**
     Copies the image to the public storage directory and returns the Uri
     */
    public static Uri saveImageForSharing(Context context, Bitmap bitmap, String imageName) {
        String subdirectory = getSubdirectoryName(context);
        File fileToWrite = new File(getExternalStoragePublicDirectory(subdirectory), imageName);
>>>>>>> origin/mergeTestBranch

        try {
            FileOutputStream outputStream = new FileOutputStream(fileToWrite);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return Uri.fromFile(fileToWrite);
        }
    }

<<<<<<< HEAD

=======
    /*
>>>>>>> origin/mergeTestBranch
    public static void saveImage(Context context, Bitmap bitmap, String name) {
        File fileDirectory = getFileDirectory(context);
        File fileToWrite = new File(fileDirectory, name);

        try {
            FileOutputStream outputStream = new FileOutputStream(fileToWrite);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */

    /**
     * Return URI from image file
     */
    public static Uri getImageUri(File imageFile) {
        return Uri.fromFile(imageFile);
    }

    /**
     * Create a File for saving an image or video
     */
<<<<<<< HEAD
    public static File saveImageFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "SnapYak");

            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (!imageStorageDir.exists()) {
                if (!imageStorageDir.mkdirs()) {
                    Log.d(TAG, "failed to create directory");
                    return null;
                }
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile = new File(imageStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");

            return mediaFile;
        } else {
            Log.d(TAG, "SD card not mounted.");
            return null;
        }
=======
    public static File saveImageFile(Context context) {
        File fileDirectory = getFileDirectory(context);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "IMG_" + timeStamp + ".jpg";
        return new File(fileDirectory.getPath() + File.separator + imageName);
>>>>>>> origin/mergeTestBranch
    }

}
