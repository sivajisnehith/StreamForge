package com.Opsfusionn.StreamForge.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.Opsfusionn.StreamForge.dto.UploadResponse;
import com.Opsfusionn.StreamForge.dto.VideoUploadRequest;
import com.Opsfusionn.StreamForge.model.Video;
import com.Opsfusionn.StreamForge.service.FileStorageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/files")
public class FileUploadController{

    private final FileStorageService fileStorageService;

    public FileUploadController(FileStorageService fileStorageService){
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(
            @RequestParam MultipartFile file, 
            @Valid @RequestPart("metadata") VideoUploadRequest metadata) throws IOException{
        Video video = fileStorageService.storeFile(file,metadata);
        UploadResponse response = new UploadResponse(video.getStoredFileName(), "Video uploaded successfully.", video.getId());
        return ResponseEntity.ok(response);
    }
}

