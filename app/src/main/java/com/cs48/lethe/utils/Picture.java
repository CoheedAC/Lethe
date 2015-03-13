package com.cs48.lethe.utils;

import java.io.File;

/**
 * Class that stores information about a picture.
 */
public class Picture {

    // Instance variables
    private String uniqueId;
    private String datePosted;
    private String thumbnailUrl;
    private String fullUrl;
    private File file;
    private double latitude;
    private double longitude;
    private int views;
    private int likes;
    private int orientation;

    /**
     * Constructor that creates a picture object
     *
     * @param uniqueId The unique ID returned by the server
     * @param latitude The latitude of where the picture was taken
     * @param longitude The longtitue of where the picture was taken
     * @param pictureFile The picture file. Null if retrieved from server.
     *                    Exists if taken from camera.
     * @param thumbnailUrl The URL for the thumbnail.
     * @param fullUrl The URL for the full-sized picture
     * @param orientation The orientation of the camera
     * @param views The number of views
     * @param likes The number of likes
     * @param datePosted The date the picture was posted to the server
     */
    public Picture(String uniqueId, double latitude, double longitude, File pictureFile, String thumbnailUrl, String fullUrl,int orientation, int views, int likes, String datePosted) {
        this.uniqueId = uniqueId;
        this.datePosted = datePosted;
        this.thumbnailUrl = thumbnailUrl;
        this.fullUrl = fullUrl;
        this.orientation = orientation;
        this.views = views;
        this.likes = likes;
        this.latitude = latitude;
        this.longitude = longitude;
        file = pictureFile;
    }

    /**
     * Gets the latitude
     *
     * @return The latitude of where the picture was taken
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Gets the longitude
     *
     * @return The longitude of where the picture was taken
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Gets the orientation
     *
     * @return The orientation of the camera when the picture
     *         was taken.
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * Gets the picture file
     *
     * @return The picture file. Null if retrieved from server.
     *         Exists if taken from camera.
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the date posted
     *
     * @return The date the picture was posted to the server
     */
    public String getDatePosted() {
        return datePosted;
    }

    /**
     * Gets the unique ID
     *
     * @return The unique ID returned by the server
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Gets the thumbnail URL
     *
     * @return The URL for the thumbnail.
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * Gets the full-sized URL
     *
     * @return The URL for the full-sized picture.
     */
    public String getFullUrl() {
        return fullUrl;
    }

    /**
     * Gets the view count
     *
     * @return The number of views
     */
    public int getViews() {
        return views;
    }

    /**
     * Sets the view count
     *
     * @param views The view count to set
     */
    public void setViews(int views) {
        this.views = views;
    }

    /**
     * Gets the like count
     *
     * @return The number of likes
     */
    public int getLikes() {
        return likes;
    }

    /**
     * Sets the liek count
     *
     * @param likes The like count to set
     */
    public void setLikes(int likes) {
        this.likes = likes;
    }

    /**
     * Increments the like count by one.
     */
    public void like() {
        likes++;
    }

    /**
     *  Increments the view count by one.
     */
    public void view() {
        views++;
    }

}
