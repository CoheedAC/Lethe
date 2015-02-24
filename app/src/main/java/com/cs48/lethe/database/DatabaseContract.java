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
    public static abstract class Table {
        public static final String COLUMN_NAME_PHOTO_ID = "photo_id";
        public static final String COLUMN_NAME_DATE_POSTED = "date_posted";
        public static final String COLUMN_NAME_VIEWS = "views";
        public static final String COLUMN_NAME_LIKES = "likes";
    }

    /* Inner class that defines the table contents */
    public static abstract class CachedImagesTable extends Table implements BaseColumns {
        public static final String TABLE_NAME = "cached_images_table";

        public static final String COLUMN_NAME_THUMBNAIL_URL = "thumbnail_url";
        public static final String COLUMN_NAME_FULL_URL = "full_url";
        public static final String COLUMN_NAME_IS_LIKED = "is_liked";
        public static final String COLUMN_NAME_VISIBILITY = "visibility";

        public static final int VISIBLE = 1;
        public static final int HIDDEN = 0;

        public static final int FALSE = 0;
        public static final int TRUE = 1;
    }

    /* Inner class that defines the table contents */
    public static abstract class PostedImagesTable extends Table implements BaseColumns {
        public static final String TABLE_NAME = "posted_images_table";

        public static final String COLUMN_NAME_FILE = "file";
    }
}