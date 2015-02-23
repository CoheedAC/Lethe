package com.cs48.lethe.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.cs48.lethe.ApplicationSettings;
import com.cs48.lethe.R;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Utility class that handles tasks related to file storage
 * and manipulation.
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
     * Returns whether there is internet connectivity or not.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Returns the current latitude[0] and longitude[1]
     */
    public static String[] getLocationCoordinates(Context context) {
        String[] coordinates = new String[2];
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(lm.getBestProvider(new Criteria(), true));

        if (location != null) {
            coordinates[0] = String.valueOf(location.getLatitude());
            coordinates[1] = String.valueOf(location.getLongitude());
        } else {
            // default to isla vista coordinates
            coordinates[0] = "34.4133"; // latitude
            coordinates[1] = "-119.861"; // longitude
        }
        return coordinates;
    }

    /**
     * Returns true if external storage is mounted. False otherwise.
     */
    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state)) ? true : false;
    }

    /**
     * Returns string of app name
     */
    public static String getSubdirectoryName(Context context) {
        return context.getResources().getString(R.string.app_name).replace(" ", "");
    }

    /**
     * Returns app subdirectory in the external public storage. If directory doesn't exist, then it's created.
     */
    public static File getSharedExternalDirectory(Context context) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + getSubdirectoryName(context));
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.d(TAG, "Failed to make directory.");
            }
        }
        return dir;
    }

    /**
     * Returns the cache directory.
     */
    public static File getCachedDirectory(Context context) {
        ApplicationSettings settings = new ApplicationSettings(context);
        String storageType = settings.getStoragePreference();
        if (storageType.equals(StorageType.INTERNAL) || !isExternalStorageAvailable()) {
            return context.getCacheDir();
        } else
            return context.getExternalCacheDir();
    }

    /**
     * Returns an array of jpg files that are saved in the storage directory
     */
    public static List<File> getPostedImages(Context context) {
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
     * Returns an array of jpg files that are saved in the storage directory
     */
    public static List<File> getCachedImages(Context context) {
        File cachedDirectory = getCachedDirectory(context);
        Log.d(TAG, cachedDirectory.getAbsolutePath());
        File[] filteredFiles = cachedDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.getAbsolutePath().contains(".jpg")) ? true : false;
            }
        });
        return new ArrayList<>(Arrays.asList(filteredFiles));
    }

    /**
     * Deletes the image from a given Uri path
     */
    public static boolean deleteImage(Uri deleteUri) {
        File imageFile = new File(deleteUri.getPath());
        return imageFile.delete();
    }

    /**
     * Copies the image to the public storage directory and returns the Uri
     */
    public static void saveImageForSharing(Context context, String imagePath) throws IOException {
        File imageSource = new File(imagePath);
        File imageDestination = new File(getSharedExternalDirectory(context) + "/" + imageSource.getName());

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
    public static File savePostedImage(Context context) {
        File fileDirectory = getFileDirectory(context);
        return new File(fileDirectory.getPath() + File.separator + getImageFileName());
    }

    /**
     * Returns a string of a file name based upon the timestamp
     */
    private static String getImageFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "IMG_" + timeStamp + ".jpg";
    }

    /**
     * Returns the file name without the extension and without the full
     * path location.
     */
    public static String getSimpleName(String absolutePath) {
        String reverse = new StringBuilder(absolutePath).reverse().toString();
        String result;
        int index = reverse.indexOf("/");
        if (index != -1) {
            result = absolutePath.substring(absolutePath.length() - index);
            index = result.indexOf("jpg");
            if (index != -1)
                result = result.substring(0, index + 3);
            return result;
        } else {
            return null;
        }
    }

    /**
     * Returns the full sized bitmap of the image.
     */
    public static Bitmap getValidSizedBitmap(ContentResolver cr, Uri mImageUri) {
        return (getXYCompressedBitmap(cr, mImageUri, 2048, 2048));
    }

    /**
     * Returns the thumbnail sized bitmap of the image.
     */
    public static Bitmap getThumbnailSizedBitmap(ContentResolver cr, Uri mImageUri) {
        return (getXYCompressedBitmap(cr, mImageUri, 150, 150));
    }

    /**
     * Returns the custom sized bitmap of the image.
     */
    public static Bitmap getXYCompressedBitmap(ContentResolver cr, Uri mImageUri, int x, int y) {
        try {
            Bitmap bp = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
            return (Bitmap.createScaledBitmap(bp, x, y, false)); //low quality

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the unique picture ID of an image file.
     * (i.e. turns "IMG_xxx.jpg" -> "xxx")
     */
    public static String getUniqueId(String filename) {
        return filename.substring(4, filename.length() - 4);
    }

}
