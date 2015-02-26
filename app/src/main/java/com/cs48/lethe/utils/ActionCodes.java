package com.cs48.lethe.utils;

/**
 * Created by maxkohne on 2/25/15.
 */
public class ActionCodes {

    // Request codes
    public static final int POST_PICTURE_REQUEST = 100;
    public static final int ME_FULL_PICTURE_REQUEST = 101;
    public static final int FEED_FULL_PICTURE_REQUEST = 102;
    public static final int PEEK_FULL_PICTURE_REQUEST = 102;
    public static final int CAMERA_CAPTURE_REQUEST = 104;

    // Result codes
    public static final int HIDE_PICTURE = 500;
    public static final int DELETE_PICTURE = 501;
    public static final int POST_SUCCESS = 502;
    public static final int POST_CANCELLED = 503;
    public static final int POST_FAILED = 504;
}
