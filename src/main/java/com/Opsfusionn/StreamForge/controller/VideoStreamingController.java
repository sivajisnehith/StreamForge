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

    public VideoStreamingController(MinioService minioService){
        this.minioService = minioService;
    }

    @GetMapping("/{videoId}/{fileName}")
    public ResponseEntity<InputStreamResource> streamVideo(
            @PathVariable UUID videoId,
            @PathVariable String fileName) {
        
        InputStream stream = minioService.getProcessedObject(videoId, fileName);
        InputStreamResource resource = new InputStreamResource(stream);
        
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (fileName.endsWith(".m3u8")) {
            mediaType = MediaType.parseMediaType("application/x-mpegURL");
        } else if (fileName.endsWith(".ts")) {
            mediaType = MediaType.parseMediaType("video/MP2T");
        }
        
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
    
    @GetMapping("/{videoId}/{rendition}/master.m3u8")
    public ResponseEntity<InputStreamResource> getMasterPlaylist(@PathVariable UUID videoId,@PathVariable String rendition){
        String objectName = rendition + "/playlist.m3u8";
        InputStream inputStream = minioService.getProcessedObject(videoId, objectName);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
            .body(new InputStreamResource(inputStream));
    }
    
}
