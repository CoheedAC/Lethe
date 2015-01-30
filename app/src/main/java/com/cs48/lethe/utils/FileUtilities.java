package com.cs48.lethe.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.cs48.lethe.ApplicationSettings;
import com.cs48.lethe.R;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

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
                return context.getFilesDir();
            }
        }
    }

    /**
     Returns true if external storage is mounted. False otherwise.
     */
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

    /**
     Returns an array of jpg files that are saved in the storage directory
     */
    public static File[] listFiles(Context context) {
        File fileDirectory = getFileDirectory(context);
        File[] filteredFiles = fileDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.getAbsolutePath().contains(".jpg")) ? true : false;
            }
        });
        return filteredFiles;
    }

    /**
    Returns app subdirectory in the external public storage. If directory doesn't exist, then it's created.
    */
    public static File getExternalStoragePublicDirectory(String subdirectory) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + subdirectory);
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
    public static String getSubdirectoryName(Context context) {
        return context.getResources().getString(R.string.app_name).replace(" ", "");
    }

    public static void deleteAllImages(Context context) {
        String sub = getSubdirectoryName(context);
        File dir = getExternalStoragePublicDirectory(sub);
        for (File file: dir.listFiles())
            file.delete();
        File dir2 = context.getExternalFilesDir(sub);
        for (File file: dir2.listFiles())
            file.delete();
    }

    public static void copyFile (Context context, String path, int numCopies) throws IOException{
        File dir = getFileDirectory(context);
        for (int i = 0; i < numCopies; i++) {
            File dst = new File(dir + "/IMG_" + (i+1) + ".jpg");

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
     Copies the image to the public storage directory and returns the Uri
     */
    public static Uri saveImageForSharing(Context context, Bitmap bitmap, String imageName) {
        String subdirectory = getSubdirectoryName(context);
        File fileToWrite = new File(getExternalStoragePublicDirectory(subdirectory), imageName);

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

    /*
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
    public static File saveImageFile(Context context) {
        File fileDirectory = getFileDirectory(context);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "IMG_" + timeStamp + ".jpg";
        return new File(fileDirectory.getPath() + File.separator + imageName);
    }

}
