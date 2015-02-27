package com.cs48.lethe.utils;

/**
 * Created by maxkohne on 2/25/15.
 */
public class ActionCodes {

    // Request codes
    public static final int POST_PICTURE_REQUEST = 100;
    public static final int ME_FULLSCREEN_REQUEST = 101;
    public static final int FEED_FULLSCREEN_REQUEST = 102;
    public static final int PEEK_FULLSCREEN_REQUEST = 102;
    public static final int CAMERA_CAPTURE_REQUEST = 104;

    // Result codes
    public static final int HIDE_PICTURE = 200;
    public static final int DELETE_PICTURE = 201;
    public static final int POST_SUCCESS = 202;
    public static final int POST_CANCELLED = 203;
    public static final int POST_FAILED = 204;
}
