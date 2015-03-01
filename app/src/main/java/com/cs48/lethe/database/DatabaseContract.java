package com.cs48.lethe.database;

import android.provider.BaseColumns;

/**
 * Created by maxkohne on 2/22/15.
 */
public final class DatabaseContract {

    /* Inner class that defines the table contents */
    public static abstract class Table {
        public static final String COLUMN_NAME_PICTURE_ID = "pictureId";
        public static final String COLUMN_NAME_DATE_POSTED = "datePosted";
        public static final String COLUMN_NAME_VIEWS = "views";
        public static final String COLUMN_NAME_LIKES = "likes";
    }

    /* Inner class that defines the table contents */
    public static abstract class FeedTable extends Table implements BaseColumns {
        public static final String TABLE_NAME = "FeedPictures";

        public static final String COLUMN_NAME_FILE_PATH = "filePath";
        public static final String COLUMN_NAME_THUMBNAIL_URL = "thumbnailUrl";
        public static final String COLUMN_NAME_FULL_URL = "fullUrl";
        public static final String COLUMN_NAME_IS_VIEWED = "isViewed";
        public static final String COLUMN_NAME_IS_LIKED = "isLiked";
        public static final String COLUMN_NAME_VISIBILITY = "visibility";

        public static final int VISIBLE = 1;
        public static final int HIDDEN = 0;

        public static final int TRUE = 1;
        public static final int FALSE = 0;
    }

    /* Inner class that defines the table contents */
    public static abstract class PeekTable extends Table implements BaseColumns {
        public static final String TABLE_NAME = "PeekPictures";

        public static final String COLUMN_NAME_THUMBNAIL_URL = "thumbnailUrl";
        public static final String COLUMN_NAME_FULL_URL = "fullUrl";
    }

    /* Inner class that defines the table contents */
    public static abstract class MeTable extends Table implements BaseColumns {
        public static final String TABLE_NAME = "MePictures";
        public static final String COLUMN_NAME_FILE_PATH = "filePath";
    }

}