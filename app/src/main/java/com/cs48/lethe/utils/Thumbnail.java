package com.cs48.lethe.utils;

import java.io.File;

/**
 * Class that stores information about a thumbnail.
 */
public class Thumbnail {

    private String url;
    private File thumbnailFile;
    private String id;

    public Thumbnail(String id, String url) {
        this.id = id;
        this.url = url;
        thumbnailFile = new File(FileUtilities.getCachedDirectory() + File.separator + "IMG_" + id + ".jpg");
    }

    public String getUrl() {
        return url;
    }

    public File getThumbnailFile() {
        return thumbnailFile;
    }

    public String getId() {
        return id;
    }
}
