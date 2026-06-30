package com.Opsfusionn.StreamForge.messaging;

import java.util.UUID;

public class VideoProcessingMessage {
    private UUID videoId;
    private String storedFileName;

    public VideoProcessingMessage() {
    }

    public VideoProcessingMessage(UUID videoId, String storedFileName) {
        this.videoId = videoId;
        this.storedFileName = storedFileName;
    }

    public UUID getVideoId() {
        return videoId;
    }

    public void setVideoId(UUID videoId) {
        this.videoId = videoId;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }
}
