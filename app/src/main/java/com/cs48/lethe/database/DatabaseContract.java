package com.cs48.lethe.database;

import android.provider.BaseColumns;

/**
 * A class that defines the various contents of the tables in the database.
 */
public class DatabaseContract {

    /**
     * Inner class that defines the global Table
     * contents that is shared across all tables
     */
    public static abstract class Table {
        // Column names for the table
        public static final String COLUMN_NAME_PICTURE_ID = "pictureId";
        public static final String COLUMN_NAME_DATE_POSTED = "datePosted";
        public static final String COLUMN_NAME_VIEWS = "views";
        public static final String COLUMN_NAME_LIKES = "likes";
    }

    /**
     * Inner class that defines the Feed Table contents which
     * also inherits the other columns from the global table.
     *
     * This table is designed to hold all picture meta-data
     * for the pictures in the user's current area that were
     * fetched from the server.
     */
    public static abstract class FeedTable extends Table implements BaseColumns {
        // Name of the Feed Table
        public static final String TABLE_NAME = "FeedPictures";

        // Column names for the table
        public static final String COLUMN_NAME_FILE_PATH = "filePath";
        public static final String COLUMN_NAME_THUMBNAIL_URL = "thumbnailUrl";
        public static final String COLUMN_NAME_FULL_URL = "fullUrl";
        public static final String COLUMN_NAME_IS_VIEWED = "isViewed";
        public static final String COLUMN_NAME_IS_LIKED = "isLiked";
        public static final String COLUMN_NAME_VISIBILITY = "visibility";

        // Boolean values for the VISIBILITY column
        public static final int VISIBLE = 1;
        public static final int HIDDEN = 0;

        // Boolean values for the IS_LIKED and IS_VIEWED columns
        public static final int TRUE = 1;
        public static final int FALSE = 0;
    }

    /**
     * Inner class that defines the Peek Table contents which
     * also inherits the other columns from the global table
     *
     * This table is designed to hold all picture meta-data
     * for the pictures in the user's desired area that were
     * fetched from the server.
     */
    public static abstract class PeekTable extends Table implements BaseColumns {
        // Name of the Peek Table
        public static final String TABLE_NAME = "PeekPictures";

        // Column names for the table
        public static final String COLUMN_NAME_THUMBNAIL_URL = "thumbnailUrl";
        public static final String COLUMN_NAME_FULL_URL = "fullUrl";
    }

    /**
     * Inner class that defines the Me Table contents which
     * also inherits the other columns from the global table
     *
     * This table is designed to hold all picture meta-data
     * for the pictures that the user has posted to the server.
     */
    public static abstract class MeTable extends Table implements BaseColumns {
        // Name of the Me table
        public static final String TABLE_NAME = "MePictures";
        public static final String COLUMN_NAME_FILE_PATH = "filePath";
    }

}