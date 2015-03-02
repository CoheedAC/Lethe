package com.cs48.lethe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cs48.lethe.database.DatabaseContract.FeedTable;
import com.cs48.lethe.database.DatabaseContract.MeTable;
import com.cs48.lethe.database.DatabaseContract.PeekTable;
import com.cs48.lethe.utils.Picture;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by maxkohne on 2/22/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper sInstance;

    // Logcat tag
    private static final String LOG_TAG = "DatabaseHelper";

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "pictureManager.db";

    // Table types
    private static final String TEXT_TYPE = " TEXT";
    private static final String DATE_TYPE = " DATE";
    private static final String INT_TYPE = " INT";
    private static final String INTEGER_DEFAULT_TYPE = " INTEGER DEFAULT ";
    private static final String INTEGER_PRIMARY_KEY_TYPE = " INTEGER PRIMARY KEY";
    private static final String COMMA_SEP = ",";

    // SQLite commands
    private static final String SELECT_ALL_FROM = "SELECT * FROM ";
    private static final String WHERE = " WHERE ";
    private static final String ORDER_BY = " ORDER BY ";
    private static final String EQUALS = " = ";
    private static final String EQUALS_WILDCARD = " = ?";
    private static final String ASCENDING = " ASC";
    private static final String DESCENDING = " DESC";

    // Table Create Statements
    // Feed table create statement
    private static final String CREATE_FEED_TABLE =
            "CREATE TABLE " + FeedTable.TABLE_NAME + " (" +
                    FeedTable._ID + INTEGER_PRIMARY_KEY_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_PICTURE_ID + TEXT_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_DATE_POSTED + DATE_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_FILE_PATH + DATE_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_THUMBNAIL_URL + TEXT_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_FULL_URL + TEXT_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_VIEWS + INT_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_LIKES + INT_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_IS_VIEWED + INTEGER_DEFAULT_TYPE + FeedTable.FALSE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_IS_LIKED + INTEGER_DEFAULT_TYPE + FeedTable.FALSE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_VISIBILITY + INTEGER_DEFAULT_TYPE + FeedTable.VISIBLE +
                    ")";

    // Peak table create statement
    private static final String CREATE_PEEK_TABLE =
            "CREATE TABLE " + PeekTable.TABLE_NAME + " (" +
                    PeekTable._ID + INTEGER_PRIMARY_KEY_TYPE + COMMA_SEP +
                    PeekTable.COLUMN_NAME_PICTURE_ID + TEXT_TYPE + COMMA_SEP +
                    PeekTable.COLUMN_NAME_DATE_POSTED + TEXT_TYPE + COMMA_SEP +
                    PeekTable.COLUMN_NAME_THUMBNAIL_URL + TEXT_TYPE + COMMA_SEP +
                    PeekTable.COLUMN_NAME_FULL_URL + TEXT_TYPE + COMMA_SEP +
                    PeekTable.COLUMN_NAME_VIEWS + INT_TYPE + COMMA_SEP +
                    PeekTable.COLUMN_NAME_LIKES + INT_TYPE +
                    ")";

    // Me table create statement
    private static final String CREATE_ME_TABLE =
            "CREATE TABLE " + MeTable.TABLE_NAME + " (" +
                    MeTable._ID + INTEGER_PRIMARY_KEY_TYPE + COMMA_SEP +
                    MeTable.COLUMN_NAME_PICTURE_ID + TEXT_TYPE + COMMA_SEP +
                    MeTable.COLUMN_NAME_DATE_POSTED + TEXT_TYPE + COMMA_SEP +
                    MeTable.COLUMN_NAME_FILE_PATH + TEXT_TYPE + COMMA_SEP +
                    MeTable.COLUMN_NAME_VIEWS + INT_TYPE + COMMA_SEP +
                    MeTable.COLUMN_NAME_LIKES + INT_TYPE +
                    ")";

    private static final String DELETE_TABLE = "DROP TABLE IF EXISTS ";

    public static DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FEED_TABLE);
        db.execSQL(CREATE_PEEK_TABLE);
        db.execSQL(CREATE_ME_TABLE);
    }

    /**
     * Called when the database needs to be upgraded. The implementation should
     * use this method to drop tables, add tables, or do anything else it needs
     * to upgrade to the new schema version. This method executes within a transaction.
     * If an exception is thrown, all changes will automatically be rolled back.
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL(DELETE_TABLE + FeedTable.TABLE_NAME);
        db.execSQL(DELETE_TABLE + PeekTable.TABLE_NAME);
        db.execSQL(DELETE_TABLE + MeTable.TABLE_NAME);

        // create new tables
        onCreate(db);
    }

    /**
     * Called when the database needs to be downgraded. This is strictly similar to
     * onUpgrade(SQLiteDatabase, int, int) method, but is called whenever current version
     * is newer than requested one. However, this method is not abstract, so it is not
     * mandatory for a customer to implement it.
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean pictureExistInFeed(Picture picture) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = SELECT_ALL_FROM + FeedTable.TABLE_NAME + WHERE
                + FeedTable.COLUMN_NAME_PICTURE_ID + EQUALS + picture.getUniqueId();

        Cursor c = db.rawQuery(selectQuery, null);
        int count = c.getCount();

        c.close();
        return (count != 0);
    }

    public boolean pictureExistInPeekFeed(Picture picture) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = SELECT_ALL_FROM + PeekTable.TABLE_NAME + WHERE
                + PeekTable.COLUMN_NAME_PICTURE_ID + EQUALS + picture.getUniqueId();

        Cursor c = db.rawQuery(selectQuery, null);
        int count = c.getCount();

        c.close();
        return (count != 0);
    }

    public void updateFeed(Map<String, Picture> pictureMap) {
        SQLiteDatabase db = getWritableDatabase();

        Iterator iterator = pictureMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            Picture picture = (Picture) pair.getValue();
            if (!pictureExistInFeed(picture)) {
                ContentValues values = new ContentValues();
                values.put(FeedTable.COLUMN_NAME_PICTURE_ID, picture.getUniqueId());
                values.put(FeedTable.COLUMN_NAME_DATE_POSTED, picture.getDatePosted());
                values.put(FeedTable.COLUMN_NAME_THUMBNAIL_URL, picture.getThumbnailUrl());
                values.put(FeedTable.COLUMN_NAME_FULL_URL, picture.getFullUrl());
                values.put(FeedTable.COLUMN_NAME_VIEWS, picture.getViews());
                values.put(FeedTable.COLUMN_NAME_LIKES, picture.getLikes());

                db.insert(FeedTable.TABLE_NAME, null, values);
            } else
                updateDatabaseFromPicture(picture);
        }
        db.close();
    }

    public void updatePeekFeed(List<Picture> pictureList) {
        SQLiteDatabase db = getWritableDatabase();

        for (Picture picture : pictureList) {
            if (!pictureExistInPeekFeed(picture)) {
                ContentValues values = new ContentValues();
                values.put(PeekTable.COLUMN_NAME_PICTURE_ID, picture.getUniqueId());
                values.put(PeekTable.COLUMN_NAME_DATE_POSTED, picture.getDatePosted());
                values.put(PeekTable.COLUMN_NAME_THUMBNAIL_URL, picture.getThumbnailUrl());
                values.put(PeekTable.COLUMN_NAME_FULL_URL, picture.getFullUrl());
                values.put(PeekTable.COLUMN_NAME_VIEWS, picture.getViews());
                values.put(PeekTable.COLUMN_NAME_LIKES, picture.getLikes());

                db.insert(PeekTable.TABLE_NAME, null, values);
            }
        }
        db.close();
    }

    public void clearFeedTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(FeedTable.TABLE_NAME, null, null);
        db.close();
    }

    public void clearPeekTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(PeekTable.TABLE_NAME, null, null);
        db.close();
    }

    public void clearMeTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MeTable.TABLE_NAME, null, null);
        db.close();
    }

    public void updateDatabaseFromPicture(Picture updatedPicture) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues feedValues = new ContentValues();
        feedValues.put(FeedTable.COLUMN_NAME_VIEWS, updatedPicture.getViews());
        feedValues.put(FeedTable.COLUMN_NAME_LIKES, updatedPicture.getLikes());

        ContentValues meValues = new ContentValues();
        meValues.put(MeTable.COLUMN_NAME_VIEWS, updatedPicture.getViews());
        meValues.put(MeTable.COLUMN_NAME_LIKES, updatedPicture.getLikes());

        ContentValues peekValues = new ContentValues();
        peekValues.put(PeekTable.COLUMN_NAME_VIEWS, updatedPicture.getViews());
        peekValues.put(PeekTable.COLUMN_NAME_LIKES, updatedPicture.getLikes());

        db.update(FeedTable.TABLE_NAME, feedValues, FeedTable.COLUMN_NAME_PICTURE_ID +
                EQUALS_WILDCARD, new String[]{updatedPicture.getUniqueId()});
        db.update(MeTable.TABLE_NAME, feedValues, MeTable.COLUMN_NAME_PICTURE_ID +
                EQUALS_WILDCARD, new String[]{updatedPicture.getUniqueId()});
        db.update(PeekTable.TABLE_NAME, peekValues, PeekTable.COLUMN_NAME_PICTURE_ID +
                EQUALS_WILDCARD, new String[]{updatedPicture.getUniqueId()});
    }

    public Picture getFeedPicture(String uniqueId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = SELECT_ALL_FROM + FeedTable.TABLE_NAME + WHERE +
                FeedTable.COLUMN_NAME_PICTURE_ID + EQUALS + uniqueId;

        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();

        db.close();

        String pictureFilePath = c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_FILE_PATH));
        File pictureFile = (pictureFilePath == null) ? null : new File(pictureFilePath);

        return new Picture(c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_PICTURE_ID)),
                c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_DATE_POSTED)),
                pictureFile,
                c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_THUMBNAIL_URL)),
                c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_FULL_URL)),
                c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_VIEWS)),
                c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_LIKES)));
    }

    public void insertPicture(Picture postedPicture) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues meValues = new ContentValues();
        meValues.put(MeTable.COLUMN_NAME_PICTURE_ID, postedPicture.getUniqueId());
        meValues.put(MeTable.COLUMN_NAME_FILE_PATH, postedPicture.getFile().getAbsolutePath());
        meValues.put(MeTable.COLUMN_NAME_DATE_POSTED, postedPicture.getDatePosted());

        ContentValues feedValues = new ContentValues();
        feedValues.put(FeedTable.COLUMN_NAME_PICTURE_ID, postedPicture.getUniqueId());
        feedValues.put(FeedTable.COLUMN_NAME_FILE_PATH, postedPicture.getFile().getAbsolutePath());
        feedValues.put(FeedTable.COLUMN_NAME_DATE_POSTED, postedPicture.getDatePosted());

        db.insert(MeTable.TABLE_NAME, null, meValues);
        db.insert(FeedTable.TABLE_NAME, null, feedValues);
        db.close();
    }

    public void deletePictureFromMeTable(Picture pictureToDelete) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (pictureToDelete.getFile() != null && pictureToDelete.getFile().exists())
            pictureToDelete.getFile().delete();

        String whereClause = MeTable.COLUMN_NAME_PICTURE_ID + EQUALS + pictureToDelete.getUniqueId();
        db.delete(MeTable.TABLE_NAME, whereClause, null);

        db.close();
    }

    public void deletePictureFromPeekTable(Picture pictureToDelete) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (pictureToDelete.getFile() != null && pictureToDelete.getFile().exists())
            pictureToDelete.getFile().delete();

        String whereClause = PeekTable.COLUMN_NAME_PICTURE_ID + EQUALS + pictureToDelete.getUniqueId();
        db.delete(PeekTable.TABLE_NAME, whereClause, null);

        db.close();
    }

    public void deletePictureFromFeedTable(Picture pictureToDelete) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (pictureToDelete.getFile() != null && pictureToDelete.getFile().exists())
            pictureToDelete.getFile().delete();

        String whereClause = FeedTable.COLUMN_NAME_PICTURE_ID + EQUALS + pictureToDelete.getUniqueId();
        db.delete(FeedTable.TABLE_NAME, whereClause, null);

        db.close();
    }

    public void deletePictureFromDatabase(Picture pictureToDelete) {
        deletePictureFromPeekTable(pictureToDelete);
        deletePictureFromFeedTable(pictureToDelete);
        deletePictureFromMeTable(pictureToDelete);
    }

    public void viewPicture(Picture picture) {
        SQLiteDatabase db = this.getWritableDatabase();
        picture.view();

        ContentValues feedValues = new ContentValues();
        feedValues.put(FeedTable.COLUMN_NAME_VIEWS, picture.getViews());
        feedValues.put(FeedTable.COLUMN_NAME_IS_VIEWED, FeedTable.TRUE);

        ContentValues meValues = new ContentValues();
        meValues.put(MeTable.COLUMN_NAME_VIEWS, picture.getViews());

        db.update(FeedTable.TABLE_NAME, feedValues, FeedTable.COLUMN_NAME_PICTURE_ID +
                EQUALS_WILDCARD, new String[]{picture.getUniqueId()});
        db.update(MeTable.TABLE_NAME, meValues, MeTable.COLUMN_NAME_PICTURE_ID +
                EQUALS_WILDCARD, new String[]{picture.getUniqueId()});
        db.close();
    }

    public void hidePicture(Picture picture) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedTable.COLUMN_NAME_VISIBILITY, FeedTable.HIDDEN);

        db.update(FeedTable.TABLE_NAME, values, FeedTable.COLUMN_NAME_PICTURE_ID +
                EQUALS_WILDCARD, new String[]{picture.getUniqueId()});
        db.close();
    }

    public void likePicture(Picture picture) {
        SQLiteDatabase db = this.getWritableDatabase();
        picture.like();

        ContentValues feedValues = new ContentValues();
        feedValues.put(FeedTable.COLUMN_NAME_LIKES, picture.getLikes());
        feedValues.put(FeedTable.COLUMN_NAME_IS_LIKED, FeedTable.TRUE);

        ContentValues meValues = new ContentValues();
        meValues.put(MeTable.COLUMN_NAME_LIKES, picture.getLikes());

        db.update(FeedTable.TABLE_NAME, feedValues, FeedTable.COLUMN_NAME_PICTURE_ID +
                EQUALS_WILDCARD, new String[]{picture.getUniqueId()});
        db.update(MeTable.TABLE_NAME, meValues, MeTable.COLUMN_NAME_PICTURE_ID +
                EQUALS_WILDCARD, new String[]{picture.getUniqueId()});
        db.close();
    }

    public boolean isPictureLiked(Picture picture) {
        SQLiteDatabase db = getWritableDatabase();

        String selectQuery = SELECT_ALL_FROM + FeedTable.TABLE_NAME + WHERE
                + FeedTable.COLUMN_NAME_PICTURE_ID + EQUALS + picture.getUniqueId();

        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();
        boolean isLiked = (c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_IS_LIKED)) == FeedTable.TRUE);
        c.close();
        db.close();
        return isLiked;
    }

    public boolean isPictureViewed(Picture picture) {
        SQLiteDatabase db = getWritableDatabase();

        String selectQuery = SELECT_ALL_FROM + FeedTable.TABLE_NAME + WHERE
                + FeedTable.COLUMN_NAME_PICTURE_ID + EQUALS + picture.getUniqueId();

        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();
        boolean isViewed = (c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_IS_VIEWED)) == FeedTable.TRUE);
        c.close();
        db.close();
        return isViewed;
    }

    public List<Picture> getFeedPictures() {
        List<Picture> pictureList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = SELECT_ALL_FROM + FeedTable.TABLE_NAME + WHERE + FeedTable.COLUMN_NAME_VISIBILITY
                + EQUALS + FeedTable.VISIBLE + ORDER_BY + FeedTable.COLUMN_NAME_DATE_POSTED + ASCENDING;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                String pictureFilePath = c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_FILE_PATH));
                File pictureFile = (pictureFilePath == null) ? null : new File(pictureFilePath);

                Picture picture = new Picture(c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_PICTURE_ID)),
                        c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_DATE_POSTED)),
                        pictureFile,
                        c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_THUMBNAIL_URL)),
                        c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_FULL_URL)),
                        c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_VIEWS)),
                        c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_LIKES)));
                pictureList.add(picture);
//                updateDatabaseFromPicture(picture);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return pictureList;
    }

    public List<Picture> getPeekPictures() {
        List<Picture> pictureList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = SELECT_ALL_FROM + PeekTable.TABLE_NAME + ORDER_BY + PeekTable.COLUMN_NAME_DATE_POSTED + ASCENDING;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Picture picture = new Picture(c.getString(c.getColumnIndex(PeekTable.COLUMN_NAME_PICTURE_ID)),
                        c.getString(c.getColumnIndex(PeekTable.COLUMN_NAME_DATE_POSTED)),
                        null,
                        c.getString(c.getColumnIndex(PeekTable.COLUMN_NAME_THUMBNAIL_URL)),
                        c.getString(c.getColumnIndex(PeekTable.COLUMN_NAME_FULL_URL)),
                        c.getInt(c.getColumnIndex(PeekTable.COLUMN_NAME_VIEWS)),
                        c.getInt(c.getColumnIndex(PeekTable.COLUMN_NAME_LIKES)));
                pictureList.add(picture);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return pictureList;
    }

    public List<Picture> getMePictures() {
        List<Picture> pictureList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = SELECT_ALL_FROM + MeTable.TABLE_NAME + ORDER_BY
                + MeTable.COLUMN_NAME_DATE_POSTED + ASCENDING;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Log.d(LOG_TAG, c.getString(c.getColumnIndex(MeTable.COLUMN_NAME_FILE_PATH)));
                pictureList.add(
                        new Picture(c.getString(c.getColumnIndex(MeTable.COLUMN_NAME_PICTURE_ID)),
                                c.getString(c.getColumnIndex(MeTable.COLUMN_NAME_DATE_POSTED)),
                                new File(c.getString(c.getColumnIndex(MeTable.COLUMN_NAME_FILE_PATH))),
                                c.getInt(c.getColumnIndex(MeTable.COLUMN_NAME_VIEWS)),
                                c.getInt(c.getColumnIndex(MeTable.COLUMN_NAME_LIKES))));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return pictureList;
    }
}
