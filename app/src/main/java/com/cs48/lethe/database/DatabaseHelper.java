package com.cs48.lethe.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cs48.lethe.database.DatabaseContract.FeedEntry;
import com.cs48.lethe.database.DatabaseContract.MeEntry;

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
                    FeedEntry.COLUMN_NAME_LIKES + INT_TYPE +
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

}
