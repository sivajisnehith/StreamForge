package com.Opsfusionn.StreamForge.controller;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.Opsfusionn.StreamForge.service.MinioService;

@RestController
@RequestMapping("/api/minio")
public class MinioTestController {

    private final MinioService minioService;

    public MinioTestController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/test-upload")
    public ResponseEntity<String> uploadTestFile(
            @RequestParam("file") MultipartFile file) {

        try {

            Path tempFile = Files.createTempFile("streamforge-", ".tmp");

            file.transferTo(tempFile);

            minioService.uploadOriginalFile(
                    file.getOriginalFilename(),
                    tempFile);

            Files.deleteIfExists(tempFile);

            return ResponseEntity.ok("File uploaded successfully to MinIO.");

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body(e.getMessage());

        }
    }
}