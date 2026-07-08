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

    private String thumbnailUrl;
    private String masterPlaylistUrl;

    private Double duration;
    private Integer width;
    private Integer height;
    private String videoCodec;
    private String audioCodec;
    private Long bitRate;
    private String title;
    private String description;

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

    public VideoResponse(UUID videoId, String originalFileName, long fileSize, String contentType, VideoStatus status, LocalDateTime uploadedAt, String thumbnailUrl, String masterPlaylistUrl, Double duration, Integer width, Integer height, String videoCodec, String audioCodec, Long bitRate) {
        this.videoId = videoId;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.status = status;
        this.uploadedAt = uploadedAt;

        this.masterPlaylistUrl = masterPlaylistUrl;
        this.thumbnailUrl = thumbnailUrl;

        this.duration = duration;
        this.width = width;
        this.height = height;
        this.videoCodec = videoCodec;
        this.audioCodec = audioCodec;
        this.bitRate = bitRate;
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getMasterPlaylistUrl() {
        return masterPlaylistUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setMasterPlaylistUrl(String masterPlaylistUrl) {
        this.masterPlaylistUrl = masterPlaylistUrl;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public void setVideoCodec(String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public Long getBitRate() {
        return bitRate;
    }

    public void setBitRate(Long bitRate) {
        this.bitRate = bitRate;
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
