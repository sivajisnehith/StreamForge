package com.Opsfusionn.StreamForge.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Video{
    private UUID id;
    private String originalFileName;
    private String storedFileName;
    private long fileSize;
    private String contentType;
    private VideoStatus status;
    private LocalDateTime uploadedAt;

    public Video(){
        
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public VideoStatus getStatus() {
        return status;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
