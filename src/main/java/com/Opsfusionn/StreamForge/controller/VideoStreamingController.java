package com.Opsfusionn.StreamForge.controller;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Opsfusionn.StreamForge.service.MinioService;

@RestController
@RequestMapping("/api/videos")
public class VideoStreamingController {

    private final MinioService minioService;

    public VideoStreamingController(MinioService minioService) {
        this.minioService = minioService;
    }

    /**
     * Returns the HLS master playlist.
     *
     * GET /api/videos/{videoId}/master.m3u8
     */
    @GetMapping("/{videoId}/{fileName:.+}")
    public ResponseEntity<InputStreamResource> streamVideo(
            @PathVariable UUID videoId,
            @PathVariable String fileName) {

        InputStream inputStream =
                minioService.getProcessedObject(videoId, fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                .body(new InputStreamResource(inputStream));
    }

    /**
     * Returns a rendition playlist.
     *
     * Example:
     * GET /api/videos/{videoId}/1080/playlist.m3u8
     */
    @GetMapping("/{videoId}/{rendition}/playlist.m3u8")
    public ResponseEntity<InputStreamResource> getRenditionPlaylist(
            @PathVariable UUID videoId,
            @PathVariable String rendition) {

        String objectName = rendition + "/playlist.m3u8";

        InputStream inputStream =
                minioService.getProcessedObject(videoId, objectName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                .body(new InputStreamResource(inputStream));
    }

    /**
     * Returns an HLS transport stream segment.
     *
     * Example:
     * GET /api/videos/{videoId}/1080/segment000.ts
     */
    @GetMapping("/{videoId}/{rendition}/{segment:.+\\.ts}")
    public ResponseEntity<InputStreamResource> getSegment(
            @PathVariable UUID videoId,
            @PathVariable String rendition,
            @PathVariable String segment) {

        String objectName = rendition + "/" + segment;

        InputStream inputStream =
                minioService.getProcessedObject(videoId, objectName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp2t"))
                .body(new InputStreamResource(inputStream));
    }

    @GetMapping("/{videoId}/thumbnail.jpg")
    public ResponseEntity<InputStreamResource> getThumbnail(@PathVariable UUID videoId) {

        InputStream inputStream =
                minioService.getProcessedObject(videoId, "thumbnail.jpg");

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(new InputStreamResource(inputStream));
    }
}