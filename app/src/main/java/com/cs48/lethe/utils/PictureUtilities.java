package com.cs48.lethe.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.IOException;

/**
 * Created by maxkohne on 2/26/15.
 */
public class PictureUtilities {

    /**
     * Rotates a bitmap by the specified degrees
     */
    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Returns the orientation of the image taken by the camera
     */
    public static int getImageOrientation(String imagePath){
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static Bitmap getValidSizedBitmap(ContentResolver cr, Uri mImageUri){
        return(getXYCompressedBitmap(cr, mImageUri,2048,2048));
    }

    public static Bitmap getThumbnailSizedBitmap(ContentResolver cr, Uri mImageUri){
        return(getXYCompressedBitmap(cr, mImageUri,150,150));
    }

    public static Bitmap getXYCompressedBitmap(ContentResolver cr, Uri mImageUri, int x, int y){
        try {
            Bitmap bp = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
            return (Bitmap.createScaledBitmap(bp,x,y,false)); //low quality

        }catch (Exception e){
            return null;
        }
    }
}
