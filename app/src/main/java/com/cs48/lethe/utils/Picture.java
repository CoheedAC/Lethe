package com.cs48.lethe.utils;

import java.io.File;

/**
 * Class that stores information about a picture.
 */
public class Picture {

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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public File getFile() {
        return file;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void like() {
        likes++;
    }

    public void view() {
        views++;
    }

}
