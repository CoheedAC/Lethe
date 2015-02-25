package com.cs48.lethe.utils;

import android.annotation.SuppressLint;
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
    @SuppressLint("SimpleDateFormat")
    public static File savePostedImage(Context context) {
        File fileDirectory = getFileDirectory(context);
        return new File(fileDirectory.getAbsolutePath(),
                "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg");
    }


    /**
     * Returns the full sized bitmap of the image.
     */
    public static Bitmap getValidSizedBitmap(Bitmap bitmap) {
        final int WIDTH = 2048;
        final int HEIGHT = 2048;

        if (bitmap.getWidth() > WIDTH || bitmap.getHeight() > HEIGHT)
            return Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, false);
        else return bitmap;
    }

    /**
     * Returns the thumbnail sized bitmap of the image.
     */
    public static Bitmap getThumbnailSizedBitmap(Bitmap bitmap) {
        final int WIDTH = 150;
        final int HEIGHT = 150;

        if (bitmap.getWidth() > WIDTH || bitmap.getHeight() > HEIGHT)
            return Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, false);
        else return bitmap;
    }

//    /**
//     * Returns the full sized bitmap of the image.
//     */
//    public static Bitmap getValidSizedBitmap(ContentResolver cr, Uri mImageUri) {
//        return (getXYCompressedBitmap(cr, mImageUri, 2048, 2048));
//    }
//
//    /**
//     * Returns the thumbnail sized bitmap of the image.
//     */
//    public static Bitmap getThumbnailSizedBitmap(ContentResolver cr, Uri mImageUri) {
//        return (getXYCompressedBitmap(cr, mImageUri, 150, 150));
//    }
//
//    public static Bitmap getXYCompressedBitmap(ContentResolver cr, Uri mImageUri, int x, int y) {
//        try {
//            Bitmap bp = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
//            return (Bitmap.createScaledBitmap(bp, x, y, false)); //low quality
//
//        } catch (Exception e) {
//            return null;
//        }
//    }

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
}
