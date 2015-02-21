package com.cs48.lethe.utils;

import java.io.File;

/**
 * Class that stores information about a full sized picture.
 */
public class FullPicture {

    private String url;
    private File file;
    private String id;
    private int views;
    private int likes;

    public FullPicture(String id, String url, int views, int likes) {
        this.id = id;
        this.url = url;
        this.views =views;
        this.likes = likes;

        file = new File(FileUtilities.getCachedDirectory() + File.separator + "IMG_" + id + ".jpg");
    }

    public String getUrl() {
        return url;
    }

    public File getFile() {
        return file;
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
