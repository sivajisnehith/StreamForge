package com.Opsfusionn.StreamForge.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    private UUID id;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, unique = true)
    private String storedFileName;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VideoStatus status;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Video() {

    }
    
    //getters
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

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setStatus(VideoStatus status) {
        this.status = status;
    }
}