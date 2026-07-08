package com.Opsfusionn.StreamForge.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.Opsfusionn.StreamForge.dto.UpdateVideoStatusRequest;
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
    public ResponseEntity<Page<VideoResponse>> getAllVideos(
        @RequestParam(required = false) String search,
        @PageableDefault(
                sort = "uploadedAt",
                direction = Sort.Direction.DESC)
                Pageable pageable) {
                    
        Page<VideoResponse> responses =
                fileStorageService.getAllVideos(search, pageable);

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<Void> deleteVideo(@PathVariable UUID videoId) throws IOException {
        
        fileStorageService.deleteVideo(videoId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{videoId}/status")
    public ResponseEntity<Void> updateVideoStatus(@PathVariable UUID videoId, @RequestBody UpdateVideoStatusRequest request) {

        fileStorageService.updateVideoStatus(videoId, request);

        return ResponseEntity.noContent().build();
    }
}
