package com.cs48.lethe.database;

import android.provider.BaseColumns;

/**
 * Created by maxkohne on 2/22/15.
 */
public final class DatabaseContract {

    /* Inner class that defines the table contents */
    public static abstract class Table {
        public static final String COLUMN_NAME_PICTURE_ID = "picture_id";
        public static final String COLUMN_NAME_DATE_POSTED = "date_posted";
        public static final String COLUMN_NAME_VIEWS = "views";
        public static final String COLUMN_NAME_LIKES = "likes";
    }

    /* Inner class that defines the table contents */
    public static abstract class FeedTable extends Table implements BaseColumns {
        public static final String TABLE_NAME = "feed_table";

        public static final String COLUMN_NAME_THUMBNAIL_URL = "thumbnail_url";
        public static final String COLUMN_NAME_FULL_URL = "full_url";
        public static final String COLUMN_NAME_IS_VIEWED = "is_viewed";
        public static final String COLUMN_NAME_IS_LIKED = "is_liked";
        public static final String COLUMN_NAME_VISIBILITY = "visibility";

        public static final int VISIBLE = 1;
        public static final int HIDDEN = 0;

        public static final int TRUE = 1;
        public static final int FALSE = 0;
    }

    /* Inner class that defines the table contents */
    public static abstract class PeekTable extends Table implements BaseColumns {
        public static final String TABLE_NAME = "peek_table";

        public static final String COLUMN_NAME_THUMBNAIL_URL = "thumbnail_url";
        public static final String COLUMN_NAME_FULL_URL = "full_url";
    }

    /* Inner class that defines the table contents */
    public static abstract class MeTable extends Table implements BaseColumns {
        public static final String TABLE_NAME = "me_table";
        public static final String COLUMN_NAME_FILE = "file";
    }

}