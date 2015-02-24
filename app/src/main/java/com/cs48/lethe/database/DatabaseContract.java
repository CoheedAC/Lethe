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

    private static abstract class Table {
        public static final String COLUMN_NAME_PHOTO_ID = "photoid";
        public static final String COLUMN_NAME_DATE_POSTED = "dateposted";
        public static final String COLUMN_NAME_VIEWS = "views";
        public static final String COLUMN_NAME_LIKES = "likes";

    }

    /* Inner class that defines the table contents */
    public static abstract class FeedTable extends Table implements BaseColumns {
        public static final String TABLE_NAME = "feedtable";

        public static final String COLUMN_NAME_THUMBNAIL_URL = "thumbnailurl";
        public static final String COLUMN_NAME_FULL_URL = "fullurl";
        public static final String COLUMN_NAME_IS_LIKED = "isliked";
        public static final String COLUMN_NAME_VISIBILITY = "visibility";

        public static final int VISIBLE = 1;
        public static final int HIDDEN = 0;

        public static final int FALSE = 0;
        public static final int TRUE = 1;
    }

    public static abstract class MeTable extends Table implements BaseColumns {
        public static final String TABLE_NAME = "metable";

        public static final String COLUMN_NAME_FILEPATH = "filepath";
    }
}