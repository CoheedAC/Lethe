package com.cs48.lethe.database;

import android.provider.BaseColumns;

/**
 * Created by maxkohne on 2/22/15.
 */
public final class DatabaseContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DatabaseContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "feedtable";
        public static final String COLUMN_NAME_PHOTO_ID = "photoid";
        public static final String COLUMN_NAME_THUMBNAIL_URL = "thumbnailurl";
        public static final String COLUMN_NAME_FULL_URL = "fullurl";
        public static final String COLUMN_NAME_VIEWS = "views";
        public static final String COLUMN_NAME_LIKES = "likes";
    }

    public static abstract class MeEntry implements BaseColumns {
        public static final String TABLE_NAME = "metable";
        public static final String COLUMN_NAME_PHOTO_ID = "photoid";
        public static final String COLUMN_NAME_POST_DATE= "dateposted";
        public static final String COLUMN_NAME_VIEWS = "views";
        public static final String COLUMN_NAME_LIKES = "likes";
    }
}