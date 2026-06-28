package com.Opsfusionn.StreamForge.dto;

import java.util.UUID;

public class UploadResponse {
    private String fileName;
    private String message;
    private UUID videoId;
    public UploadResponse(String filename, String message,UUID videoId) {
        this.fileName = filename;
        this.message = message;
        this.videoId = videoId;
    }

    public String getMessage() {
        return message;
    }

    public String getFileName() {
        return fileName;
    }

    public UUID getVideoId() {
        return videoId;
    }

}
