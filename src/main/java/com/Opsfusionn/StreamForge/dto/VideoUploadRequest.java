package com.Opsfusionn.StreamForge.dto;

public class VideoUploadRequest {

    private String title;
    private String description;

    public VideoUploadRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}