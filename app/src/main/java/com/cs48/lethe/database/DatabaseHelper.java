package com.cs48.lethe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cs48.lethe.database.DatabaseContract.FeedEntry;
import com.cs48.lethe.database.DatabaseContract.MeEntry;
import com.cs48.lethe.utils.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxkohne on 2/22/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

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
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_PHOTO_ID + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_THUMBNAIL_URL + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_FULL_URL + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_VIEWS + INT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_LIKES + INT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_VISIBILITY + " INTEGER DEFAULT " + FeedEntry.VISIBLE +
                    ")";

    // Me table create statement
    private static final String CREATE_TABLE_ME =
            "CREATE TABLE " + MeEntry.TABLE_NAME + " (" +
                    MeEntry._ID + " INTEGER PRIMARY KEY," +
                    MeEntry.COLUMN_NAME_PHOTO_ID + TEXT_TYPE + COMMA_SEP +
                    MeEntry.COLUMN_NAME_POST_DATE + TEXT_TYPE + COMMA_SEP +
                    MeEntry.COLUMN_NAME_VIEWS + INT_TYPE + COMMA_SEP +
                    MeEntry.COLUMN_NAME_LIKES + INT_TYPE +
                    ")";

    private static final String DELETE_TABLE = "DROP TABLE IF EXISTS ";

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
        db.execSQL(DELETE_TABLE + FeedEntry.TABLE_NAME);
        db.execSQL(DELETE_TABLE + MeEntry.TABLE_NAME);

        // create new tables
        onCreate(db);
    }

    public boolean imageExist(String uniqueId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + FeedEntry.TABLE_NAME + " WHERE "
            + FeedEntry.COLUMN_NAME_PHOTO_ID + " = " + uniqueId;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.getCount() == 0)
            return false;
        return true;
    }

    public void updateImage(String uniqueId, int likes, int views) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_VIEWS, views);
        values.put(FeedEntry.COLUMN_NAME_LIKES, likes);

        db.update(FeedEntry.TABLE_NAME, values, FeedEntry.COLUMN_NAME_PHOTO_ID + " = ?", new String[] {uniqueId});
    }

    public void createFeedTable(List<Image> imageList) {
        Log.d(LOG, "createFeedTable called");
        SQLiteDatabase db = this.getWritableDatabase();

        for (Image image : imageList) {
            ContentValues values = new ContentValues();
            values.put(FeedEntry.COLUMN_NAME_PHOTO_ID, image.getUniqueId());
            values.put(FeedEntry.COLUMN_NAME_THUMBNAIL_URL, image.getThumbnailUrl());
            values.put(FeedEntry.COLUMN_NAME_FULL_URL, image.getFullUrl());
            values.put(FeedEntry.COLUMN_NAME_VIEWS, image.getViews());
            values.put(FeedEntry.COLUMN_NAME_LIKES, image.getLikes());

            boolean exist = imageExist(image.getUniqueId());
            if (!exist) {
                Log.d(LOG, image.getUniqueId() + " does not exist");
                db.insert(FeedEntry.TABLE_NAME, null, values);
            }else {
                Log.d(LOG, image.getUniqueId() + "  exists");
                updateImage(image.getUniqueId(), image.getLikes(), image.getViews());
            }
        }
    }

    public void hideImage(String uniqueId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_VISIBILITY, FeedEntry.HIDDEN);

        db.update(FeedEntry.TABLE_NAME, values, FeedEntry.COLUMN_NAME_PHOTO_ID + " = ?", new String[] {uniqueId});
    }

    public List<Image> fetchImageList() {
        List<Image> imageList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + FeedEntry.TABLE_NAME + " WHERE " + FeedEntry.COLUMN_NAME_VISIBILITY
                + " = " + FeedEntry.VISIBLE;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                String uniqueId = c.getString(c.getColumnIndex(FeedEntry.COLUMN_NAME_PHOTO_ID));
                String thumbnailUrl = c.getString(c.getColumnIndex(FeedEntry.COLUMN_NAME_THUMBNAIL_URL));
                String fullUrl = c.getString(c.getColumnIndex(FeedEntry.COLUMN_NAME_FULL_URL));
                int views = c.getInt(c.getColumnIndex(FeedEntry.COLUMN_NAME_VIEWS));
                int likes = c.getInt(c.getColumnIndex(FeedEntry.COLUMN_NAME_LIKES));

                imageList.add(new Image(uniqueId, thumbnailUrl, fullUrl, views, likes));
            } while (c.moveToNext());
        }

        return imageList;
    }

}
