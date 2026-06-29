package com.Opsfusionn.StreamForge.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import com.Opsfusionn.StreamForge.model.VideoStatus;

public class VideoResponse {
    private UUID videoId;
    private String originalFileName;
    private long fileSize;
    private String contentType;
    private VideoStatus status;
    private LocalDateTime uploadedAt;

    public VideoResponse() {
    }

    public VideoResponse(UUID videoId, String originalFileName, long fileSize, String contentType, VideoStatus status, LocalDateTime uploadedAt) {
        this.videoId = videoId;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.status = status;
        this.uploadedAt = uploadedAt;
    }

    public UUID getVideoId() {
        return videoId;
    }

    public void setVideoId(UUID videoId) {
        this.videoId = videoId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public VideoStatus getStatus() {
        return status;
    }

    public void setStatus(VideoStatus status) {
        this.status = status;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
