package com.Opsfusionn.StreamForge.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Video{
    UUID id;
    String originalFileName;
    String storedFileName;
    String originalPath;
    long fileSize;
    String contentType;
    VideoStatus status;
    LocalDateTime uploadedAt;
}
