package com.cs48.lethe.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cs48.lethe.database.DatabaseContract.FeedTable;
import com.cs48.lethe.database.DatabaseContract.MeTable;
import com.cs48.lethe.database.DatabaseContract.PeekTable;
import com.cs48.lethe.utils.Picture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A class the deals with interacting with the internal
 * database that stores all of the picture meta-data.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String TAG = DatabaseHelper.class.getSimpleName();

    // Class variable for the database helper
    private static DatabaseHelper sInstance;

    // Database version
    private static final int DATABASE_VERSION = 2;

    // Database name
    private static final String DATABASE_NAME = "PictureManager.db";

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
    private static final String DESCENDING = " DESC";

    /* Table Create Statements */
    // Feed table create statement
    private static final String CREATE_FEED_TABLE =
            "CREATE TABLE " + FeedTable.TABLE_NAME + " (" +
                    FeedTable._ID + INTEGER_PRIMARY_KEY_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_PICTURE_ID + TEXT_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_DATE_POSTED + DATE_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_FILE_PATH + DATE_TYPE + COMMA_SEP +
                    FeedTable.COLUMN_NAME_ORIENTATION + INT_TYPE + COMMA_SEP +
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
                    PeekTable.COLUMN_NAME_ORIENTATION + INT_TYPE + COMMA_SEP +
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
                    MeTable.COLUMN_NAME_ORIENTATION + INT_TYPE + COMMA_SEP +
                    MeTable.COLUMN_NAME_VIEWS + INT_TYPE + COMMA_SEP +
                    MeTable.COLUMN_NAME_LIKES + INT_TYPE +
                    ")";

    // Drop table statement
    private static final String DELETE_TABLE = "DROP TABLE IF EXISTS ";

    /**
     * Gets the picture meta-data database helper
     *
     * @param context Interface to global information about an application environment
     * @return The database helper.
     */
    public static DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (sInstance == null)
            sInstance = new DatabaseHelper(context.getApplicationContext());
        return sInstance;
    }

    /**
     * Constructor that creates the database helper
     *
     * @param context Interface to global information about an application environment
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. If the tables
     * already exist, the tables will not be recreated. Otherwise, creates
     * Feed, Peek, and Me Tables.
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
     * Called when the database needs to be upgraded.This method executes within
     * a transaction. If an exception is thrown, all changes will automatically
     * be rolled back.
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Deletes all of the tables in the database
        db.execSQL(DELETE_TABLE + FeedTable.TABLE_NAME);
        db.execSQL(DELETE_TABLE + PeekTable.TABLE_NAME);
        db.execSQL(DELETE_TABLE + MeTable.TABLE_NAME);

        // Creates new tables
        onCreate(db);
    }

    /**
     * Called when the database needs to be downgraded. This is strictly similar to
     * onUpgrade(SQLiteDatabase, int, int) method, but is called whenever current version
     * is newer than requested one.
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Gets the list of pictures in the Feed Table
     *
     * @return The list of pictures in the Feed Table
     *         that are not hidden and sorted by
     *         the date posted to the server
     */
    public List<Picture> getFeedPictures() {
        // List of pictures to return
        List<Picture> pictureList = new ArrayList<>();

        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // Select query to find all of the pictures that have not been hidden in the Feed Table
        // sorted by the the date posted to the server
        String selectQuery = SELECT_ALL_FROM + FeedTable.TABLE_NAME + WHERE + FeedTable.COLUMN_NAME_VISIBILITY
                + EQUALS + FeedTable.VISIBLE + ORDER_BY + FeedTable.COLUMN_NAME_DATE_POSTED + DESCENDING;

        // Reads the query
        Cursor c = db.rawQuery(selectQuery, null);

        // Moves to the first row in the query
        if (c.moveToFirst()) {
            // For each row in the query, add the picture to the list
            do {
                // Get the file of the picture if it exists
                String pictureFilePath = c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_FILE_PATH));
                File pictureFile = (pictureFilePath == null) ? null : new File(pictureFilePath);

                pictureList.add(new Picture(c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_PICTURE_ID)),
                        c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_DATE_POSTED)),
                        pictureFile,
                        c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_THUMBNAIL_URL)),
                        c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_FULL_URL)),
                        c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_ORIENTATION)),
                        c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_VIEWS)),
                        c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_LIKES))));
            } while (c.moveToNext());
        }

        // Closes the Cursor, releasing all of its resources and making it completely invalid.
        c.close();

        // Releases the database resources
        db.close();

        return pictureList;
    }

    /**
     * Gets the list of pictures in the Peek Table
     *
     * @return The list of pictures in the Peek Table
     *         sorted by the date posted to the server
     */
    public List<Picture> getPeekPictures() {
        // List of pictures to return
        List<Picture> pictureList = new ArrayList<>();

        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // Select query to find all of the pictures in the Peek Table sorted by the the date posted to the server
        String selectQuery = SELECT_ALL_FROM + PeekTable.TABLE_NAME + ORDER_BY + PeekTable.COLUMN_NAME_DATE_POSTED + DESCENDING;

        // Reads the query
        Cursor c = db.rawQuery(selectQuery, null);

        // Moves to the first row in the query
        if (c.moveToFirst()) {
            // For each row in the query, add the picture to the list
            do {
                pictureList.add(new Picture(c.getString(c.getColumnIndex(PeekTable.COLUMN_NAME_PICTURE_ID)),
                        c.getString(c.getColumnIndex(PeekTable.COLUMN_NAME_DATE_POSTED)),
                        null,
                        c.getString(c.getColumnIndex(PeekTable.COLUMN_NAME_THUMBNAIL_URL)),
                        c.getString(c.getColumnIndex(PeekTable.COLUMN_NAME_FULL_URL)),
                        c.getInt(c.getColumnIndex(PeekTable.COLUMN_NAME_ORIENTATION)),
                        c.getInt(c.getColumnIndex(PeekTable.COLUMN_NAME_VIEWS)),
                        c.getInt(c.getColumnIndex(PeekTable.COLUMN_NAME_LIKES))));
            } while (c.moveToNext());
        }

        // Closes the Cursor, releasing all of its resources and making it completely invalid.
        c.close();

        // Releases the database resources
        db.close();

        return pictureList;
    }

    /**
     * Gets the list of pictures in the Me Table
     * which is the list of pictures that the
     * user has posted to the server.
     *
     * @return The list of pictures in the Feed Table
     *         sorted by the date posted to the server
     */
    public List<Picture> getMePictures() {
        // List of pictures to return
        List<Picture> pictureList = new ArrayList<>();

        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // Select query to find all of the pictures in the Me Table sorted by the the date posted to the server
        String selectQuery = SELECT_ALL_FROM + MeTable.TABLE_NAME + ORDER_BY
                + MeTable.COLUMN_NAME_DATE_POSTED + DESCENDING;

        // Reads the query
        Cursor c = db.rawQuery(selectQuery, null);

        // Moves to the first row in the query
        if (c.moveToFirst()) {
            // For each row in the query, add the picture to the list
            do {
                pictureList.add(
                        new Picture(c.getString(c.getColumnIndex(MeTable.COLUMN_NAME_PICTURE_ID)),
                                c.getString(c.getColumnIndex(MeTable.COLUMN_NAME_DATE_POSTED)),
                                new File(c.getString(c.getColumnIndex(MeTable.COLUMN_NAME_FILE_PATH))),
                                c.getInt(c.getColumnIndex(MeTable.COLUMN_NAME_ORIENTATION)),
                                c.getInt(c.getColumnIndex(MeTable.COLUMN_NAME_VIEWS)),
                                c.getInt(c.getColumnIndex(MeTable.COLUMN_NAME_LIKES))));
            } while (c.moveToNext());
        }

        // Closes the Cursor, releasing all of its resources and making it completely invalid.
        c.close();

        // Releases the database resources
        db.close();

        return pictureList;
    }

    /**
     * Updates the feed table with the new values of the HashMap
     * of pictures. If a picture in the map doesn't exist
     * in the table, then it is inserted into the Feed Table.
     * Otherwise, the values in the table are updated.
     *
     * @param pictureMap The HashMap of pictures that the
     *                   table will update from
     */
    public void updateFeed(Map<String, Picture> pictureMap) {
        // Iterates over each entry of the picture HashMap
        for (Map.Entry entry : pictureMap.entrySet()) {
            Picture picture = (Picture) entry.getValue();

            // If the picture doesn't exist in the table
            // then insert into the feed table
            if (!pictureExistInFeed(picture)) {
                // Create and/or open a database that will be used for reading and writing
                SQLiteDatabase db = getWritableDatabase();

                // Creates the values to store into the table
                ContentValues values = new ContentValues();
                values.put(FeedTable.COLUMN_NAME_PICTURE_ID, picture.getUniqueId());
                values.put(FeedTable.COLUMN_NAME_DATE_POSTED, picture.getDatePosted());
                values.put(FeedTable.COLUMN_NAME_THUMBNAIL_URL, picture.getThumbnailUrl());
                values.put(FeedTable.COLUMN_NAME_FULL_URL, picture.getFullUrl());
                values.put(FeedTable.COLUMN_NAME_ORIENTATION, picture.getOrientation());
                values.put(FeedTable.COLUMN_NAME_VIEWS, picture.getViews());
                values.put(FeedTable.COLUMN_NAME_LIKES, picture.getLikes());

                // Inserts the new values into the feed table
                db.insert(FeedTable.TABLE_NAME, null, values);

                // Releases the database resources
                db.close();
            } else {
                // Else the picture already exists, so update
                // the database (all tables) with the new values
                updateDatabaseFromPicture(picture);
            }
        }
    }

    /**
     * Updates the Peek table with the new values of the list
     * of pictures. If a picture in the list doesn't exist
     * in the table, then it is inserted into the Peek Table.
     * Otherwise, the values in the table are updated.
     *
     * @param pictureList The List of pictures that the table
     *                    will update from
     */
    public void updatePeekFeed(List<Picture> pictureList) {
        // Iterates over each picture in the list
        for (Picture picture : pictureList) {

            // If the picture doesn't exist in the table
            // then insert into the Peek Table
            if (!pictureExistInPeekFeed(picture)) {
                // Create and/or open a database that will be used for reading and writing
                SQLiteDatabase db = getWritableDatabase();

                // Creates the values to store into the table
                ContentValues values = new ContentValues();
                values.put(PeekTable.COLUMN_NAME_PICTURE_ID, picture.getUniqueId());
                values.put(PeekTable.COLUMN_NAME_DATE_POSTED, picture.getDatePosted());
                values.put(PeekTable.COLUMN_NAME_THUMBNAIL_URL, picture.getThumbnailUrl());
                values.put(PeekTable.COLUMN_NAME_FULL_URL, picture.getFullUrl());
                values.put(PeekTable.COLUMN_NAME_ORIENTATION, picture.getOrientation());
                values.put(PeekTable.COLUMN_NAME_VIEWS, picture.getViews());
                values.put(PeekTable.COLUMN_NAME_LIKES, picture.getLikes());

                // Inserts the new values into the feed table
                db.insert(PeekTable.TABLE_NAME, null, values);

                // Releases the database resources
                db.close();
            } else {
                // Else the picture already exists, so update
                // the database (all tables) with the new values
                updateDatabaseFromPicture(picture);
            }
        }
    }

    /**
     * Updates all of the tables in the database with the data
     * from a picture.
     *
     * @param updatedPicture The picture with the updated
     *                       information
     */
    public void updateDatabaseFromPicture(Picture updatedPicture) {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = getWritableDatabase();

        // Creates the values to store into the Feed Table
        ContentValues feedValues = new ContentValues();
        feedValues.put(FeedTable.COLUMN_NAME_VIEWS, updatedPicture.getViews());
        feedValues.put(FeedTable.COLUMN_NAME_LIKES, updatedPicture.getLikes());

        // Creates the values to store into the Me Table
        ContentValues meValues = new ContentValues();
        meValues.put(MeTable.COLUMN_NAME_VIEWS, updatedPicture.getViews());
        meValues.put(MeTable.COLUMN_NAME_LIKES, updatedPicture.getLikes());

        // Creates the values to store into the Peek Table
        ContentValues peekValues = new ContentValues();
        peekValues.put(PeekTable.COLUMN_NAME_VIEWS, updatedPicture.getViews());
        peekValues.put(PeekTable.COLUMN_NAME_LIKES, updatedPicture.getLikes());

        // Updates each table with their respective values
        db.update(FeedTable.TABLE_NAME, feedValues, FeedTable.COLUMN_NAME_PICTURE_ID +
                EQUALS + updatedPicture.getUniqueId(), null);
        db.update(MeTable.TABLE_NAME, meValues, MeTable.COLUMN_NAME_PICTURE_ID +
                EQUALS + updatedPicture.getUniqueId(), null);
        db.update(PeekTable.TABLE_NAME, peekValues, PeekTable.COLUMN_NAME_PICTURE_ID +
                EQUALS + updatedPicture.getUniqueId(), null);

        // Releases the database resources
        db.close();
    }

    /**
     * Checks to see if the picture already exists in the Feed Table.
     *
     * @param picture The picture to check
     * @return True if it exists. False otherwise.
     */
    public boolean pictureExistInFeed(Picture picture) {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // Select query to find the picture in the Feed Table
        String selectQuery = SELECT_ALL_FROM + FeedTable.TABLE_NAME + WHERE
                + FeedTable.COLUMN_NAME_PICTURE_ID + EQUALS + picture.getUniqueId();

        // Reads the query
        Cursor c = db.rawQuery(selectQuery, null);

        // Gets the number of rows based upon the query
        int count = c.getCount();

        // Closes the Cursor, releasing all of its resources and making it completely invalid.
        c.close();

        // Releases the database resources
        db.close();

        // If the count is 0, then the picture doesn't exit. Otherwise, it exists.
        return (count != 0);
    }

    /**
     * Checks to see if the picture already exists in the Peek Table.
     *
     * @param picture The picture to check
     * @return True if it exists. False otherwise.
     */
    public boolean pictureExistInPeekFeed(Picture picture) {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // Select query to find the picture in the Peek Table
        String selectQuery = SELECT_ALL_FROM + PeekTable.TABLE_NAME + WHERE
                + PeekTable.COLUMN_NAME_PICTURE_ID + EQUALS + picture.getUniqueId();

        // Reads the query
        Cursor c = db.rawQuery(selectQuery, null);

        // Gets the number of rows based upon the query
        int count = c.getCount();

        // Closes the Cursor, releasing all of its resources and making it completely invalid.
        c.close();

        // Releases the database resources
        db.close();

        // If the count is 0 then the picture doesn't exit.
        // Otherwise, it exists.
        return (count != 0);
    }

    /**
     * Clears all elements in the Feed Table.
     */
    public void clearFeedTable() {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = getWritableDatabase();

        // Deletes all elements in the Feed Table
        db.delete(FeedTable.TABLE_NAME, null, null);

        // Releases the database resources
        db.close();
    }

    /**
     * Clears all elements in the Peek Table.
     */
    public void clearPeekTable() {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = getWritableDatabase();

        // Deletes all elements in the Peek Table
        db.delete(PeekTable.TABLE_NAME, null, null);

        // Releases the database resources
        db.close();
    }

    /**
     * Clears all elements in the Me Table
     */
    public void clearMeTable() {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = getWritableDatabase();

        // Deletes all elements in the Me Table
        db.delete(MeTable.TABLE_NAME, null, null);

        // Releases the database resources
        db.close();
    }

    /**
     * Gets the picture in the Feed Table given a picture ID.
     *
     * @param uniqueId The picture ID to find in the table
     * @return The picture associated with the picture ID
     */
    public Picture getFeedPicture(String uniqueId) {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // Select query to find the picture in the Feed Table
        String selectQuery = SELECT_ALL_FROM + FeedTable.TABLE_NAME + WHERE +
                FeedTable.COLUMN_NAME_PICTURE_ID + EQUALS + uniqueId;

        // Reads the query
        Cursor c = db.rawQuery(selectQuery, null);

        // Picture to return
        Picture picture = null;

        // Moves to the first row
        if (c.moveToFirst()) {
            // Get the file if the path exists
            String pictureFilePath = c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_FILE_PATH));
            File pictureFile = (pictureFilePath == null) ? null : new File(pictureFilePath);

            // Create a new picture from the data in the Feed Table
            picture = new Picture(c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_PICTURE_ID)),
                    c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_DATE_POSTED)),
                    pictureFile,
                    c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_THUMBNAIL_URL)),
                    c.getString(c.getColumnIndex(FeedTable.COLUMN_NAME_FULL_URL)),
                    c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_ORIENTATION)),
                    c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_VIEWS)),
                    c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_LIKES)));
        }

        // Closes the Cursor, releasing all of its resources and making it completely invalid.
        c.close();

        // Releases the database resources
        db.close();

        return picture;
    }

    /**
     * Inserts a picture in the Feed and Me Tables.
     *
     * @param postedPicture The picture to be inserted
     */
    public void insertPicture(Picture postedPicture) {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = getWritableDatabase();

        // Creates the values to store into the Me Table
        ContentValues values = new ContentValues();
        values.put(MeTable.COLUMN_NAME_PICTURE_ID, postedPicture.getUniqueId());
        values.put(MeTable.COLUMN_NAME_FILE_PATH, postedPicture.getFile().getAbsolutePath());
        values.put(MeTable.COLUMN_NAME_DATE_POSTED, postedPicture.getDatePosted());
        values.put(MeTable.COLUMN_NAME_ORIENTATION, postedPicture.getOrientation());

        // Inserts the values into the respective tables
        // (other columns will go to their default values)
        db.insert(MeTable.TABLE_NAME, null, values);
        db.insert(FeedTable.TABLE_NAME, null, values);

        // Releases the database resources
        db.close();
    }

    /**
     * Deletes a picture from the Me Table.
     *
     * @param pictureToDelete The picture to delete
     */
    public void deletePictureFromMeTable(Picture pictureToDelete) {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // Deletes the picture file if it exists
        if (pictureToDelete.getFile() != null && pictureToDelete.getFile().exists())
            pictureToDelete.getFile().delete();

        // The constraint of the picture deletion (the unique ID's have to match)
        String whereClause = MeTable.COLUMN_NAME_PICTURE_ID + EQUALS + pictureToDelete.getUniqueId();

        // Deletes the picture from the Me Table
        db.delete(MeTable.TABLE_NAME, whereClause, null);

        // Releases the database resources
        db.close();
    }

    /**
     * Deletes a picture from the Peek Table.
     *
     * @param pictureToDelete The picture to delete
     */
    public void deletePictureFromPeekTable(Picture pictureToDelete) {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // The constraint of the picture deletion (the unique ID's have to match)
        String whereClause = PeekTable.COLUMN_NAME_PICTURE_ID + EQUALS + pictureToDelete.getUniqueId();

        // Deletes the picture from the Peek Table
        db.delete(PeekTable.TABLE_NAME, whereClause, null);

        // Releases the database resources
        db.close();
    }

    /**
     * Deletes a picture from the Feed Table.
     *
     * @param pictureToDelete The picture to delete
     */
    public void deletePictureFromFeedTable(Picture pictureToDelete) {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // The constraint of the picture deletion (the unique ID's have to match)
        String whereClause = FeedTable.COLUMN_NAME_PICTURE_ID + EQUALS + pictureToDelete.getUniqueId();

        // Deletes the picture from the Feed Table
        db.delete(FeedTable.TABLE_NAME, whereClause, null);

        // Releases the database resources
        db.close();
    }

    /**
     * Deletes a picture from all of the tables in the database.
     *
     * @param pictureToDelete The picture to delete
     */
    public void deletePictureFromDatabase(Picture pictureToDelete) {
        deletePictureFromPeekTable(pictureToDelete);
        deletePictureFromFeedTable(pictureToDelete);
        deletePictureFromMeTable(pictureToDelete);
    }

    /**
     * Increments the view count by one for a picture in the database.
     *
     * @param picture The viewed picture
     */
    public void viewPicture(Picture picture) {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // Increments the view count for the picture by one
        picture.view();

        // Creates the values to store into the Feed Table
        ContentValues feedValues = new ContentValues();
        feedValues.put(FeedTable.COLUMN_NAME_VIEWS, picture.getViews());
        feedValues.put(FeedTable.COLUMN_NAME_IS_VIEWED, FeedTable.TRUE);

        // Creates the values to store into the Me Table
        ContentValues meValues = new ContentValues();
        meValues.put(MeTable.COLUMN_NAME_VIEWS, picture.getViews());

        // Updates each tables with the respective values
        db.update(FeedTable.TABLE_NAME, feedValues, FeedTable.COLUMN_NAME_PICTURE_ID +
                EQUALS + picture.getUniqueId(), null);
        db.update(MeTable.TABLE_NAME, meValues, MeTable.COLUMN_NAME_PICTURE_ID +
                EQUALS + picture.getUniqueId(), null);

        // Releases the database resources
        db.close();
    }

    /**
     * Flags the picture to be hidden from the user.
     *
     * @param picture The picture to flag hidden
     */
    public void hidePicture(Picture picture) {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // Creates the values to store into the Feed Table
        ContentValues values = new ContentValues();
        values.put(FeedTable.COLUMN_NAME_VISIBILITY, FeedTable.HIDDEN);

        // Updates the Feed Table with the new values
        db.update(FeedTable.TABLE_NAME, values, FeedTable.COLUMN_NAME_PICTURE_ID +
                EQUALS + picture.getUniqueId(), null);

        // Releases the database resources
        db.close();
    }

    /**
     * Increments the like count by one for a picture in the database.
     *
     * @param picture The picture to like
     */
    public void likePicture(Picture picture) {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // Increments the like count for the picture by one
        picture.like();

        // Creates the values to store into the Feed Table
        ContentValues feedValues = new ContentValues();
        feedValues.put(FeedTable.COLUMN_NAME_LIKES, picture.getLikes());
        feedValues.put(FeedTable.COLUMN_NAME_IS_LIKED, FeedTable.TRUE);

        // Creates the values to store into the Me Table
        ContentValues meValues = new ContentValues();
        meValues.put(MeTable.COLUMN_NAME_LIKES, picture.getLikes());

        // Updates the tables with the respective values
        db.update(FeedTable.TABLE_NAME, feedValues, FeedTable.COLUMN_NAME_PICTURE_ID +
                EQUALS + picture.getUniqueId(), null);
        db.update(MeTable.TABLE_NAME, meValues, MeTable.COLUMN_NAME_PICTURE_ID +
                EQUALS + picture.getUniqueId(), null);

        // Releases the database resources
        db.close();
    }

    /**
     * Checks to see if the picture has already been liked.
     *
     * @param picture The picture to check.
     * @return True if the picture is already liked. Otherwise, false.
     */
    public boolean isPictureLiked(Picture picture) {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = getWritableDatabase();

        // Select query to find the picture in the Feed Table
        String selectQuery = SELECT_ALL_FROM + FeedTable.TABLE_NAME + WHERE
                + FeedTable.COLUMN_NAME_PICTURE_ID + EQUALS + picture.getUniqueId();

        // Reads the query
        Cursor c = db.rawQuery(selectQuery, null);

        // Boolean to return
        boolean isLiked = false;

        // Go to the first row in the query and get the isLiked value
        if (c.moveToFirst())
            isLiked = (c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_IS_LIKED)) == FeedTable.TRUE);

        // Closes the Cursor, releasing all of its resources and making it completely invalid.
        c.close();

        // Releases the database resources
        db.close();

        return isLiked;
    }

    /**
     * Checks to see if the picture has already been viewed.
     *
     * @param picture The picture to check.
     * @return True if the picture is already viewed. Otherwise, false.
     */
    public boolean isPictureViewed(Picture picture) {
        // Create and/or open a database that will be used for reading and writing
        SQLiteDatabase db = getWritableDatabase();

        // Select query to find the picture in the Feed Table
        String selectQuery = SELECT_ALL_FROM + FeedTable.TABLE_NAME + WHERE
                + FeedTable.COLUMN_NAME_PICTURE_ID + EQUALS + picture.getUniqueId();

        // Reads the query
        Cursor c = db.rawQuery(selectQuery, null);

        // Boolean to return
        boolean isViewed = false;

        // Go to the first row in the query and get the isViewed value
        if (c.moveToFirst())
            isViewed = (c.getInt(c.getColumnIndex(FeedTable.COLUMN_NAME_IS_VIEWED)) == FeedTable.TRUE);

        // Closes the Cursor, releasing all of its resources and making it completely invalid.
        c.close();

        // Releases the database resources
        db.close();

        return isViewed;
    }
}
