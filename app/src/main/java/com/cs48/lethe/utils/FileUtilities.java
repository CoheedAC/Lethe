package com.cs48.lethe.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.cs48.lethe.ApplicationSettings;
import com.cs48.lethe.R;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by maxkohne on 1/27/15.
 */
public class FileUtilities {

    public static final String TAG = FileUtilities.class.getSimpleName();

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
            return context.getFilesDir();
        } else if (storageType.equals(StorageType.PRIVATE_EXTERNAL)) {
            /**
             This is like getFilesDir() in that these files will be deleted
             when the application is uninstalled. However, external files are
             not always available: they will disappear if the user mounts the
             external storage on a computer or removes it.
             */
            return context.getExternalFilesDir(getSubdirectoryName(context));
        } else {
            /**
             This is where the user will typically place and manage their own files,
             so you should be careful about what you put here to ensure you don't erase
             their files or get in the way of their own organization.
             */
            return getExternalStoragePublicDirectory(context);
        }
    }

    /**
     * Returns true if external storage is mounted. False otherwise.
     */
    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state)) ? true : false;
    }

    /**
     * Returns an array of jpg files that are saved in the storage directory
     */
    public static List<File> listFiles(Context context) {
        File fileDirectory = getFileDirectory(context);
        File[] filteredFiles = fileDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.getAbsolutePath().contains(".jpg")) ? true : false;
            }
        });
        return new ArrayList<File>(Arrays.asList(filteredFiles));
    }

    /**
     * Returns app subdirectory in the external public storage. If directory doesn't exist, then it's created.
     */
    public static File getExternalStoragePublicDirectory(Context context) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + getSubdirectoryName(context));
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.d(TAG, "Failed to make directory.");
            }
        }
        return dir;
    }

    /**
     * Returns string of app name
     */
    public static String getSubdirectoryName(Context context) {
        return context.getResources().getString(R.string.app_name).replace(" ", "");
    }

    /**
     * Deletes all images in all directories related to the app.
     * (i.e. private and public external storage).
     */
    public static void deleteAllImages(Context context) {
        String sub = getSubdirectoryName(context);
        File dir = getExternalStoragePublicDirectory(context);
        for (File file : dir.listFiles())
            file.delete();
        File dir2 = context.getExternalFilesDir(sub);
        for (File file : dir2.listFiles())
            file.delete();
    }

    /**
     * Deletes the image from a given Uri path
     */
    public static boolean deleteImage(Context context, Uri deleteUri) {
        File imageFile = new File(deleteUri.getPath());
        return imageFile.delete();
    }

    /**
     * Copies a file numCopies amount of times in the same directory.
     */
    public static void copyFile(Context context, String path, int numCopies) throws IOException {
        File dir = getFileDirectory(context);
        for (int i = 0; i < numCopies; i++) {
            File dst = new File(dir + "/IMG_" + (i + 1) + ".jpg");

            InputStream in = new FileInputStream(path);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    /**
     * Copies the image to the public storage directory and returns the Uri
     */
    public static void saveImageForSharing(Context context, String imagePath) throws IOException {
        File imageSource = new File(imagePath);
        File imageDestination = new File(getExternalStoragePublicDirectory(context) + "/" + imageSource.getName());

        FileInputStream inStream = new FileInputStream(imageSource);
        FileOutputStream outStream = new FileOutputStream(imageDestination);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    /**
     * Create a File for saving an image or video
     */
    public static File saveImageFile(Context context) {
        File fileDirectory = getFileDirectory(context);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "IMG_" + timeStamp + ".jpg";
        return new File(fileDirectory.getPath() + File.separator + imageName);
    }

}
