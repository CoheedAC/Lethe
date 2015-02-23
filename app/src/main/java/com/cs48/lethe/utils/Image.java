package com.cs48.lethe.utils;

import java.io.File;
import java.io.Serializable;

/**
 * Class that stores information about a full sized picture.
 */
public class Image implements Serializable {

    private String url;
    private File file;
    private String id;
    private int views;
    private int likes;
    private int position;
    private boolean isFullSized;
    private boolean hidden;
    private boolean liked;

    public Image(String id, String url, int position){
        this.id = id;
        this.url = url;
        this.position = position;

        views = 0;
        likes = 0;
        isFullSized = false;
        hidden = false;
        liked = false;
//        file = new File(FileUtilities.getCachedDirectory(context) + File.separator + "IMG_" + id + ".jpg");
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

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setFullSized() {
        isFullSized = true;
    }

    public boolean isFullSized() {
        return isFullSized;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void hide() {
        hidden = true;
    }

    public boolean isLiked() {
        return liked;
    }

    public void like() {
        liked = true;
    }
}
