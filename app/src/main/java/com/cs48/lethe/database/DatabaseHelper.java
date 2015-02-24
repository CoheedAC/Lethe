package com.cs48.lethe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cs48.lethe.database.DatabaseContract.CachedImagesTable;
import com.cs48.lethe.database.DatabaseContract.PostedImagesTable;
import com.cs48.lethe.utils.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxkohne on 2/22/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper sInstance;

    // Logcat tag
    private static final String LOG_TAG = "DatabaseHelper";

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
    private static final String CREATE_CACHED_IMAGES_TABLE =
            "CREATE TABLE " + CachedImagesTable.TABLE_NAME + " (" +
                    CachedImagesTable._ID + " INTEGER PRIMARY KEY," +
                    CachedImagesTable.COLUMN_NAME_PHOTO_ID + TEXT_TYPE + COMMA_SEP +
                    CachedImagesTable.COLUMN_NAME_DATE_POSTED + TEXT_TYPE + COMMA_SEP +
                    CachedImagesTable.COLUMN_NAME_THUMBNAIL_URL + TEXT_TYPE + COMMA_SEP +
                    CachedImagesTable.COLUMN_NAME_FULL_URL + TEXT_TYPE + COMMA_SEP +
                    CachedImagesTable.COLUMN_NAME_VIEWS + INT_TYPE + COMMA_SEP +
                    CachedImagesTable.COLUMN_NAME_LIKES + INT_TYPE + COMMA_SEP +
                    CachedImagesTable.COLUMN_NAME_VISIBILITY + " INTEGER DEFAULT " + CachedImagesTable.VISIBLE + COMMA_SEP +
                    CachedImagesTable.COLUMN_NAME_IS_LIKED + " INTEGER DEFAULT " + CachedImagesTable.FALSE +
                    ")";

    // Me table create statement
    private static final String CREATE_POSTED_IMAGES_TABLE =
            "CREATE TABLE " + PostedImagesTable.TABLE_NAME + " (" +
                    PostedImagesTable._ID + " INTEGER PRIMARY KEY," +
                    PostedImagesTable.COLUMN_NAME_PHOTO_ID + TEXT_TYPE + COMMA_SEP +
                    PostedImagesTable.COLUMN_NAME_DATE_POSTED + TEXT_TYPE + COMMA_SEP +
                    PostedImagesTable.COLUMN_NAME_FILE + TEXT_TYPE + COMMA_SEP +
                    PostedImagesTable.COLUMN_NAME_VIEWS + INT_TYPE + COMMA_SEP +
                    PostedImagesTable.COLUMN_NAME_LIKES + INT_TYPE +
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
        db.execSQL(CREATE_CACHED_IMAGES_TABLE);
        db.execSQL(CREATE_POSTED_IMAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL(DELETE_TABLE + CachedImagesTable.TABLE_NAME);
        db.execSQL(DELETE_TABLE + PostedImagesTable.TABLE_NAME);

        // create new tables
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean imageExistInCache(Image image) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + CachedImagesTable.TABLE_NAME + " WHERE "
                + CachedImagesTable.COLUMN_NAME_PHOTO_ID + " = " + image.getUniqueId();

        Cursor c = db.rawQuery(selectQuery, null);
        int count = c.getCount();

        c.close();
        return (count != 0);
    }

    public void updateCache(List<Image> imageList) {
        SQLiteDatabase db = getWritableDatabase();

        for (Image image : imageList) {
            if (!imageExistInCache(image)) {
                ContentValues values = new ContentValues();
                values.put(CachedImagesTable.COLUMN_NAME_PHOTO_ID, image.getUniqueId());
                values.put(CachedImagesTable.COLUMN_NAME_DATE_POSTED, image.getDatePosted());
                values.put(CachedImagesTable.COLUMN_NAME_THUMBNAIL_URL, image.getThumbnailUrl());
                values.put(CachedImagesTable.COLUMN_NAME_FULL_URL, image.getFullUrl());
                values.put(CachedImagesTable.COLUMN_NAME_VIEWS, image.getViews());
                values.put(CachedImagesTable.COLUMN_NAME_LIKES, image.getLikes());

                db.insert(CachedImagesTable.TABLE_NAME, null, values);
            } else
                updateDatabaseStatisticsFromImage(image);
        }
        db.close();
    }

    public void clearCachedImages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(CachedImagesTable.TABLE_NAME, null, null);
        db.close();
    }

    public void clearPostedImages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(PostedImagesTable.TABLE_NAME, null, null);
        db.close();
    }

    public void updateDatabaseStatisticsFromImage(Image updatedImage) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues feedValues = new ContentValues();
        feedValues.put(CachedImagesTable.COLUMN_NAME_VIEWS, updatedImage.getViews());
        feedValues.put(CachedImagesTable.COLUMN_NAME_LIKES, updatedImage.getLikes());

        ContentValues postedValues = new ContentValues();
        postedValues.put(PostedImagesTable.COLUMN_NAME_VIEWS, updatedImage.getViews());
        postedValues.put(PostedImagesTable.COLUMN_NAME_LIKES, updatedImage.getLikes());

        db.update(CachedImagesTable.TABLE_NAME, feedValues, CachedImagesTable.COLUMN_NAME_PHOTO_ID + " = ?", new String[]{updatedImage.getUniqueId()});
        db.update(PostedImagesTable.TABLE_NAME, feedValues, PostedImagesTable.COLUMN_NAME_PHOTO_ID + " = ?", new String[]{updatedImage.getUniqueId()});
    }

    public void updateImageStatisticsFromDatabase(Image imageToUpdate) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + CachedImagesTable.TABLE_NAME + " WHERE " + CachedImagesTable.COLUMN_NAME_PHOTO_ID
                + " = " + imageToUpdate.getUniqueId();

        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();

        imageToUpdate.setViews(c.getInt(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_VIEWS)));
        imageToUpdate.setLikes(c.getInt(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_LIKES)));

        db.close();
    }

    public Image getCachedImage(String uniqueId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + CachedImagesTable.TABLE_NAME + " WHERE " + CachedImagesTable.COLUMN_NAME_PHOTO_ID
                + " = " + uniqueId;

        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();

        db.close();
        return new Image(c.getString(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_PHOTO_ID)),
                c.getString(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_DATE_POSTED)),
                c.getString(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_THUMBNAIL_URL)),
                c.getString(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_FULL_URL)),
                c.getInt(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_VIEWS)),
                c.getInt(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_LIKES)));
    }

    public Image getPostedImage(String uniqueId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + PostedImagesTable.TABLE_NAME + " WHERE " + PostedImagesTable.COLUMN_NAME_PHOTO_ID
                + " = " + uniqueId;

        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();
        db.close();

        return new Image(c.getString(c.getColumnIndex(PostedImagesTable.COLUMN_NAME_PHOTO_ID)),
                c.getString(c.getColumnIndex(PostedImagesTable.COLUMN_NAME_DATE_POSTED)),
                new File(c.getString(c.getColumnIndex(PostedImagesTable.COLUMN_NAME_FILE))),
                c.getInt(c.getColumnIndex(PostedImagesTable.COLUMN_NAME_VIEWS)),
                c.getInt(c.getColumnIndex(PostedImagesTable.COLUMN_NAME_LIKES)));
    }

    public void insertPostedImage(Image postedImage) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PostedImagesTable.COLUMN_NAME_PHOTO_ID, postedImage.getUniqueId());
        values.put(PostedImagesTable.COLUMN_NAME_FILE, postedImage.getFile().getAbsolutePath());
        values.put(PostedImagesTable.COLUMN_NAME_DATE_POSTED, postedImage.getDatePosted());

        db.insert(PostedImagesTable.TABLE_NAME, null, values);
        db.close();
    }

    public void deletePostedImage(Image imageToDelete) {
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = PostedImagesTable.COLUMN_NAME_PHOTO_ID + " = " + imageToDelete.getUniqueId();
        String[] whereArgs = new String[]{imageToDelete.getUniqueId()};
        db.delete(PostedImagesTable.TABLE_NAME, whereClause, null);

        db.close();
    }

    public void viewImage(Image image) {
        SQLiteDatabase db = this.getWritableDatabase();
        image.view();

        ContentValues feedValues = new ContentValues();
        feedValues.put(CachedImagesTable.COLUMN_NAME_VIEWS, image.getViews());

        ContentValues postedValues = new ContentValues();
        postedValues.put(PostedImagesTable.COLUMN_NAME_VIEWS, image.getViews());

        db.update(CachedImagesTable.TABLE_NAME, feedValues, CachedImagesTable.COLUMN_NAME_PHOTO_ID + " = ?", new String[]{image.getUniqueId()});
        db.update(PostedImagesTable.TABLE_NAME, postedValues, PostedImagesTable.COLUMN_NAME_PHOTO_ID + " = ?", new String[]{image.getUniqueId()});
        db.close();
    }

    public void hideImage(Image image) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CachedImagesTable.COLUMN_NAME_VISIBILITY, CachedImagesTable.HIDDEN);

        db.update(CachedImagesTable.TABLE_NAME, values, CachedImagesTable.COLUMN_NAME_PHOTO_ID + " = ?", new String[]{image.getUniqueId()});
        db.close();
    }

    public void likeImage(Image image) {
        SQLiteDatabase db = this.getWritableDatabase();
        image.like();

        ContentValues values = new ContentValues();
        values.put(CachedImagesTable.COLUMN_NAME_LIKES, image.getLikes());
        values.put(CachedImagesTable.COLUMN_NAME_IS_LIKED, CachedImagesTable.TRUE);

        db.update(CachedImagesTable.TABLE_NAME, values, CachedImagesTable.COLUMN_NAME_PHOTO_ID + " = ?", new String[]{image.getUniqueId()});
        db.close();
    }

    public boolean isImageLiked(Image image) {
        SQLiteDatabase db = getWritableDatabase();

        String selectQuery = "SELECT * FROM " + CachedImagesTable.TABLE_NAME + " WHERE "
                + CachedImagesTable.COLUMN_NAME_PHOTO_ID + " = " + image.getUniqueId();

        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();
        boolean isLiked = (c.getInt(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_IS_LIKED)) == CachedImagesTable.TRUE);
        c.close();
        db.close();
        return isLiked;
    }

    public List<Image> getCachedImages() {
        List<Image> imageList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + CachedImagesTable.TABLE_NAME + " WHERE " + CachedImagesTable.COLUMN_NAME_VISIBILITY
                + " = " + CachedImagesTable.VISIBLE + " ORDER BY " + CachedImagesTable.COLUMN_NAME_DATE_POSTED + " ASC";

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Image image = new Image(c.getString(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_PHOTO_ID)),
                        c.getString(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_DATE_POSTED)),
                        c.getString(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_THUMBNAIL_URL)),
                        c.getString(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_FULL_URL)),
                        c.getInt(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_VIEWS)),
                        c.getInt(c.getColumnIndex(CachedImagesTable.COLUMN_NAME_LIKES)));
                imageList.add(image);
                updateDatabaseStatisticsFromImage(image);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return imageList;
    }

    public List<Image> getPostedImages() {
        List<Image> imageList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + PostedImagesTable.TABLE_NAME + " ORDER BY " + PostedImagesTable.COLUMN_NAME_DATE_POSTED + " ASC";

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Log.d(LOG_TAG, c.getString(c.getColumnIndex(PostedImagesTable.COLUMN_NAME_FILE)));
                imageList.add(
                        new Image(c.getString(c.getColumnIndex(PostedImagesTable.COLUMN_NAME_PHOTO_ID)),
                                c.getString(c.getColumnIndex(PostedImagesTable.COLUMN_NAME_DATE_POSTED)),
                                new File(c.getString(c.getColumnIndex(PostedImagesTable.COLUMN_NAME_FILE))),
                                c.getInt(c.getColumnIndex(PostedImagesTable.COLUMN_NAME_VIEWS)),
                                c.getInt(c.getColumnIndex(PostedImagesTable.COLUMN_NAME_LIKES))));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return imageList;
    }

}
