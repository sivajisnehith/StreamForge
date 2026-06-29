package com.Opsfusionn.StreamForge.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Opsfusionn.StreamForge.dto.VideoResponse;
import com.Opsfusionn.StreamForge.service.FileStorageService;

@RestController
@RequestMapping("/api/videos")
public class VideoController {
    
    private final FileStorageService fileStorageService;

    public VideoController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoResponse> getVideoById(@PathVariable UUID videoId) {
        VideoResponse response = fileStorageService.getVideoById(videoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VideoResponse>> getAllVideos() {
        List<VideoResponse> responses = fileStorageService.getAllVideos();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<Void> deleteVideo(@PathVariable UUID videoId) throws IOException {
        
        fileStorageService.deleteVideo(videoId);

        return ResponseEntity.noContent().build();
    }

}
