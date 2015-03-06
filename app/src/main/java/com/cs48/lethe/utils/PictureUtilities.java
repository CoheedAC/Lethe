package com.cs48.lethe.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.IOException;

import static android.graphics.Paint.Align;
import static android.graphics.Paint.Style;

/**
 * Created by maxkohne on 2/26/15.
 */
public class PictureUtilities {

    public static final int MAX_FULL_WIDTH = 1024;
    public static final int MAX_FULL_HEIGHT = 768;
    public static final int MAX_THUMBNAIL_WIDTH = 205;
    public static final int MAX_THUMBNAIL_HEIGHT = 154;

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

    private BitmapDrawable writeTextOnDrawable(Context context, int drawableId, String text) {

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(convertToPixels(context, 11));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(context, 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);

        return new BitmapDrawable(context.getResources(), bm);
    }

    public static int convertToPixels(Context context, int nDP)
    {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }
}
