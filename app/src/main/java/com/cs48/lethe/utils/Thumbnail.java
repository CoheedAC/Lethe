package com.cs48.lethe.utils;

import java.io.File;

/**
 * Created by maxkohne on 2/5/15.
 */
public class Thumbnail {

    private String url;
    private File thumbnailFile;
    private String id;

    public Thumbnail(String id, String url) {
        this.id = id;
        this.url = url;

//        String filename = FileUtilities.getFileName(url);
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
