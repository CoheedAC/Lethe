package com.cs48.lethe.utils;

import java.io.File;

/**
 * Class that stores information about a thumbnail.
 */
public class Thumbnail {

    private String url;
    private File file;
    private String id;

    public Thumbnail(String id, String url) {
        this.id = id;
        this.url = url;
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
}
