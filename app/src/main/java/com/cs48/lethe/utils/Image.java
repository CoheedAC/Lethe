package com.cs48.lethe.utils;

import java.io.File;

/**
 * Class that stores information about a full sized picture.
 */
public class Image {

    private String thumbnailUrl;
    private String fullUrl;
    private String uniqueId;
    private String datePosted;

    private int views;
    private int likes;
    private File file;

    public Image(String uniqueId, String datePosted, File imageFile, int views, int likes) {
        this(uniqueId, datePosted, null, null, views, likes);
        file = imageFile;
    }

    public Image(String uniqueId, String datePosted, String thumbnailUrl, String fullUrl, int views, int likes) {
        this.uniqueId = uniqueId;
        this.datePosted = datePosted;
        this.thumbnailUrl = thumbnailUrl;
        this.fullUrl = fullUrl;
        this.views = views;
        this.likes = likes;
        file = null;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
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
