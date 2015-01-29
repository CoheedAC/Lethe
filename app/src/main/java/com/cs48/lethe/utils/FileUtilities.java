package com.cs48.lethe.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.cs48.lethe.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by maxkohne on 1/27/15.
 */
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
                return context.getFilesDir();
            }
        }
    }

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

    public static File[] listFiles(Context context) {
        File fileDirectory = getFileDirectory(context);
        File [] filteredFiles = fileDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.getAbsolutePath().contains(".jpg")) {
                    return true;
                }else
                    return false;
            }
        });
        return filteredFiles;
    }

    public static Uri saveImageForSharing(Context context, Bitmap bitmap,  String assetName) {
        File fileToWrite = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), assetName);

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
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), context.getResources().getString(R.string.app_name));
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
    }

}
