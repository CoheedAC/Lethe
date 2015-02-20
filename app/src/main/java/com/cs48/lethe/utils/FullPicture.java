package com.cs48.lethe.utils;

import java.io.File;

/**
 * Created by maxkohne on 2/20/15.
 */
public class FullPicture {

    private String url;
    private File fullPicture;
    private String id;
    private int views;
    private int likes;

    public FullPicture(String id, String url, int views, int likes) {
        this.id = id;
        this.url = url;
        this.views =views;
        this.likes = likes;

        fullPicture = new File(FileUtilities.getCachedDirectory() + File.separator + "IMG_" + id + ".jpg");
    }

    public String getUrl() {
        return url;
    }

    public File getFullPicture() {
        return fullPicture;
    }

    public String getId() {
        return id;
    }

    public int getLikes() {
        return likes;
    }

    public int getViews() {
        return views;
    }

}
