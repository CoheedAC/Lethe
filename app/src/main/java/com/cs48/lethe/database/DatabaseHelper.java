package com.cs48.lethe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cs48.lethe.database.DatabaseContract.FeedTable;
import com.cs48.lethe.database.DatabaseContract.MeTable;
import com.cs48.lethe.utils.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxkohne on 2/22/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper sInstance;;

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "imageManager.db";

    // Common table types
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INT";
    private static final String COMMA_SEP = ",";

    // Table Create Statements
    // Feed table create statement
    private static final String CREATE_TABLE_FEED =
            "CREATE TABLE " + FeedTable.TABLE_NAME + " (" +
                    FeedTable._ID + " INTEGER PRIMARY KEY," +
                    FeedTable.COLUMN_NAME_PHOTO_ID + TEXT_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_DATE_POSTED + TEXT_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_THUMBNAIL_URL + TEXT_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_FULL_URL + TEXT_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_VIEWS + INT_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_LIKES + INT_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_VISIBILITY + " INTEGER DEFAULT " + FeedTable.VISIBLE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_IS_LIKED + " INTEGER DEFAULT " + FeedTable.FALSE +
                    ")";

    // Me table create statement
    private static final String CREATE_TABLE_ME =
            "CREATE TABLE " + MeTable.TABLE_NAME + " (" +
                    MeTable._ID + " INTEGER PRIMARY KEY," +
                    MeTable.COLUMN_NAME_PHOTO_ID + TEXT_TYPE + COMMA_SEP +
                    MeTable.COLUMN_NAME_DATE_POSTED + TEXT_TYPE + COMMA_SEP +
                    MeTable.COLUMN_NAME_FILEPATH + TEXT_TYPE + COMMA_SEP +
                    MeTable.COLUMN_NAME_VIEWS + INT_TYPE + COMMA_SEP +
                    MeTable.COLUMN_NAME_LIKES + INT_TYPE +
                    ")";

    private static final String DELETE_TABLE = "DROP TABLE IF EXISTS ";

    public static DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FEED);
        db.execSQL(CREATE_TABLE_ME);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL(DELETE_TABLE + FeedTable.TABLE_NAME);
        db.execSQL(DELETE_TABLE + MeTable.TABLE_NAME);

        // create new tables
        onCreate(db);
    }

    public boolean imageExistInFeed(Image image) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + FeedTable.TABLE_NAME + " WHERE "
                + FeedTable.COLUMN_NAME_PHOTO_ID + " = " + image.getUniqueId();

        Cursor c = db.rawQuery(selectQuery, null);
        int count = c.getCount();

        c.close();
        return (count != 0);
    }

    public void updateFeedFromImages(List<Image> imageList) {
        SQLiteDatabase db = getWritableDatabase();

        for (Image image : imageList) {
            if (!imageExistInFeed(image)) {
                ContentValues values = new ContentValues();
                values.put(FeedTable.COLUMN_NAME_PHOTO_ID, image.getUniqueId());
                values.put(FeedTable.COLUMN_NAME_DATE_POSTED, image.getDatePosted());
                values.put(FeedTable.COLUMN_NAME_THUMBNAIL_URL, image.getThumbnailUrl());
                values.put(FeedTable.COLUMN_NAME_FULL_URL, image.getFullUrl());
                values.put(FeedTable.COLUMN_NAME_VIEWS, image.getViews());
                values.put(FeedTable.COLUMN_NAME_LIKES, image.getLikes());

                db.insert(FeedTable.TABLE_NAME, null, values);
            }else
                updateDatabaseStatisticsFromImage(image);
        }
        db.close();
    }

    public void clearFeed() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(FeedTable.TABLE_NAME,null,null);
        db.close();
    }

    public void clearPostedImages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MeTable.TABLE_NAME,null,null);
        db.close();
    }

    public void updateDatabaseStatisticsFromImage(Image updatedImage) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues feedValues = new ContentValues();
        feedValues.put(FeedTable.COLUMN_NAME_VIEWS, updatedImage.getViews());
        feedValues.put(FeedTable.COLUMN_NAME_LIKES, updatedImage.getLikes());

        ContentValues postedValues = new ContentValues();
        postedValues.put(MeTable.COLUMN_NAME_VIEWS, updatedImage.getViews());
        postedValues.put(MeTable.COLUMN_NAME_LIKES, updatedImage.getLikes());

        db.update(FeedTable.TABLE_NAME, feedValues, FeedTable.COLUMN_NAME_PHOTO_ID + " = ?", new String[]{updatedImage.getUniqueId()});
        db.update(MeTable.TABLE_NAME, feedValues, MeTable.COLUMN_NAME_PHOTO_ID + " = ?", new String[]{updatedImage.getUniqueId()});
    }

    public void updateImageStatisticsFromDatabase(Image imageToUpdate) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + FeedTable.TABLE_NAME + " WHERE " + FeedTable.COLUMN_NAME_PHOTO_ID
                + " = " + imageToUpdate.getUniqueId();

        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();

        imageToUpdate.setViews(c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_VIEWS)));
        imageToUpdate.setLikes(c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_LIKES)));

        db.close();
    }

    public Image getImage(String uniqueId, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (tableName.equals(FeedTable.TABLE_NAME)) {

            String selectQuery = "SELECT * FROM " + FeedTable.TABLE_NAME + " WHERE " + FeedTable.COLUMN_NAME_PHOTO_ID
                    + " = " + uniqueId;

            Cursor c = db.rawQuery(selectQuery, null);
            c.moveToFirst();

            db.close();
            return new Image(c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_PHOTO_ID)),
                    c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_DATE_POSTED)),
                    c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_THUMBNAIL_URL)),
                    c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_FULL_URL)),
                    c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_VIEWS)),
                    c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_LIKES)));
        }else {
            String selectQuery = "SELECT * FROM " + MeTable.TABLE_NAME + " WHERE " + MeTable.COLUMN_NAME_PHOTO_ID
                    + " = " + uniqueId;

            Cursor c = db.rawQuery(selectQuery, null);
            c.moveToFirst();
            db.close();

            return new Image(c.getString(c.getColumnIndex(MeTable.COLUMN_NAME_PHOTO_ID)),
                    c.getString(c.getColumnIndex(MeTable.COLUMN_NAME_DATE_POSTED)),
                    new File(c.getString(c.getColumnIndex(MeTable.COLUMN_NAME_FILEPATH))),
                    c.getInt(c.getColumnIndex(MeTable.COLUMN_NAME_VIEWS)),
                    c.getInt(c.getColumnIndex(MeTable.COLUMN_NAME_LIKES)));
        }
    }

    public void insertPostedImage(Image postedImage) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MeTable.COLUMN_NAME_PHOTO_ID, postedImage.getUniqueId());
        values.put(MeTable.COLUMN_NAME_FILEPATH, postedImage.getFile().getAbsolutePath());
        values.put(MeTable.COLUMN_NAME_DATE_POSTED, postedImage.getDatePosted());

        db.insert(MeTable.TABLE_NAME, null, values);
        db.close();
    }

    public void deletePostedImage(Image imageToDelete) {
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = MeTable.COLUMN_NAME_PHOTO_ID + " = " + imageToDelete.getUniqueId();
        String[] whereArgs = new String[] { imageToDelete.getUniqueId() };
        db.delete(MeTable.TABLE_NAME, whereClause, null);

        db.close();
    }

    public void viewImage(Image image) {
        SQLiteDatabase db = this.getWritableDatabase();
        image.view();

        ContentValues feedValues = new ContentValues();
        feedValues.put(FeedTable.COLUMN_NAME_VIEWS, image.getViews());

        ContentValues postedValues = new ContentValues();
        postedValues.put(MeTable.COLUMN_NAME_VIEWS, image.getViews());

        db.update(FeedTable.TABLE_NAME, feedValues, FeedTable.COLUMN_NAME_PHOTO_ID + " = ?", new String[]{image.getUniqueId()});
        db.update(MeTable.TABLE_NAME, postedValues, MeTable.COLUMN_NAME_PHOTO_ID + " = ?", new String[]{image.getUniqueId()});
        db.close();
    }

    public void hideImage(Image imageToHide) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedTable.COLUMN_NAME_VISIBILITY, FeedTable.HIDDEN);

        db.update(FeedTable.TABLE_NAME, values, FeedTable.COLUMN_NAME_PHOTO_ID + " = ?", new String[]{imageToHide.getUniqueId()});
        db.close();
    }

    public void likeImage(Image image) {
        SQLiteDatabase db = this.getWritableDatabase();
        image.like();

        ContentValues values = new ContentValues();
        values.put(FeedTable.COLUMN_NAME_LIKES, image.getLikes());
        values.put(FeedTable.COLUMN_NAME_IS_LIKED, FeedTable.TRUE);

        db.update(FeedTable.TABLE_NAME, values, FeedTable.COLUMN_NAME_PHOTO_ID + " = ?", new String[]{image.getUniqueId()});
        db.close();
    }

    public boolean isImageLiked(Image image) {
        SQLiteDatabase db = getWritableDatabase();

        String selectQuery = "SELECT * FROM " + FeedTable.TABLE_NAME + " WHERE "
                + FeedTable.COLUMN_NAME_PHOTO_ID + " = " + image.getUniqueId();

        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();
        boolean isLiked = (c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_IS_LIKED)) == FeedTable.TRUE);
        c.close();
        db.close();
        return isLiked;
    }

    public List<Image> getImages(String tableName) {
        List<Image> imageList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        if (tableName.equals(FeedTable.TABLE_NAME)) {

            String selectQuery = "SELECT * FROM " + FeedTable.TABLE_NAME + " WHERE " + FeedTable.COLUMN_NAME_VISIBILITY
                    + " = " + FeedTable.VISIBLE;

            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                do {

                    Image image = new Image(c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_PHOTO_ID)),
                            c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_DATE_POSTED)),
                            c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_THUMBNAIL_URL)),
                            c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_FULL_URL)),
                            c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_VIEWS)),
                            c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_LIKES)));
                    imageList.add(image);
                    updateDatabaseStatisticsFromImage(image );
                } while (c.moveToNext());
            }
            c.close();
        }else {
            String selectQuery = "SELECT * FROM " + MeTable.TABLE_NAME;

            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                do {
                    Log.d(LOG, c.getString(c.getColumnIndex(MeTable.COLUMN_NAME_FILEPATH)));
                    imageList.add(
                            new Image(c.getString(c.getColumnIndex(MeTable.COLUMN_NAME_PHOTO_ID)),
                                    c.getString(c.getColumnIndex(MeTable.COLUMN_NAME_DATE_POSTED)),
                                    new File(c.getString(c.getColumnIndex(MeTable.COLUMN_NAME_FILEPATH))),
                                    c.getInt(c.getColumnIndex(MeTable.COLUMN_NAME_VIEWS)),
                                    c.getInt(c.getColumnIndex(MeTable.COLUMN_NAME_LIKES))));
                } while (c.moveToNext());
            }
            c.close();
        }
        db.close();
        return imageList;
    }

}
