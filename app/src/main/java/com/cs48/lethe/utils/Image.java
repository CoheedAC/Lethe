package com.cs48.lethe.utils;

import java.io.File;
import java.io.Serializable;

/**
 * Class that stores information about a full sized picture.
 */
public class Image implements Serializable {

    private String thumbnailUrl;
    private String fullUrl;
    private String id;
    private int views;
    private int likes;

    private boolean isFullSized;
    private boolean hidden;
    private boolean isLiked;
    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Image(String fullUrl) {
        this("", fullUrl, fullUrl, 0, 0);
        file = new File(fullUrl);

    }

    public Image(String id, String thumbnailUrl, String fullUrl, int views, int likes) {
        this.id = id;
        this.thumbnailUrl = thumbnailUrl;
        this.fullUrl = fullUrl;
        this.views = views;
        this.likes = likes;

        isFullSized = false;
        hidden = false;
        isLiked = false;
        file = null;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isFullSized() {
        return isFullSized;
    }

    public void setFullSized(boolean isFullSized) {
        this.isFullSized = isFullSized;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void hide() {
        this.hidden = true;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void like() {
        this.likes++;
        this.isLiked = true;
    }

}
