package com.Opsfusionn.StreamForge.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.Opsfusionn.StreamForge.dto.VideoMetadata;
import com.Opsfusionn.StreamForge.exception.VideoNotFoundException;
import com.Opsfusionn.StreamForge.messaging.VideoProcessingMessage;
import com.Opsfusionn.StreamForge.model.Video;
import com.Opsfusionn.StreamForge.model.VideoStatus;
import com.Opsfusionn.StreamForge.repository.VideoRepository;

/**
 * Service orchestrating the processing workflow of uploaded videos.
 */
@Service
public class VideoProcessingService {
    private final VideoRepository videoRepository;
    private final FFmpegService ffmpegService;
    private final FFProbeService ffProbeService;
    private final MasterPlaylistService masterPlaylistService;
    private final MinioService minioService;
    
    private static final Logger logger = LoggerFactory.getLogger(VideoProcessingService.class);

    @Value("${streamforge.storage.upload-dir}")
    private String uploadDir;

    @Value("${streamforge.storage.processed-dir}")
    private String processedDir;

    public VideoProcessingService(VideoRepository videoRepository, 
                                  FFmpegService ffmpegService, 
                                  FFProbeService ffProbeService,
                                  MasterPlaylistService masterPlaylistService,
                                  MinioService minioService) {
        this.videoRepository = videoRepository;
        this.ffmpegService = ffmpegService;
        this.ffProbeService = ffProbeService;
        this.masterPlaylistService = masterPlaylistService;
        this.minioService = minioService;
    }

    /**
     * Processes a video message: updates database state to PROCESSING, invokes FFmpeg to generate 
     * HLS playlists/segments, invokes FFprobe to extract metadata, and saves the updated state to PostgreSQL.
     */
    public void processVideo(VideoProcessingMessage message) throws Exception {
        Optional<Video> videoOptional = videoRepository.findById(message.getVideoId());
        if (videoOptional.isEmpty()) {
            throw new VideoNotFoundException("Video not found.");
        }

        Video video = videoOptional.get();
        video.setStatus(VideoStatus.PENDING);
        videoRepository.save(video);

        logger.info("Video {} status updated to PENDING", video.getId());

        // Artificial delay so status change to PENDING is visible on the dashboard
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        video.setStatus(VideoStatus.PROCESSING);
        videoRepository.save(video);
        
        logger.info("Video {} status updated to PROCESSING", video.getId());

        // Artificial delay so status change to PROCESSING is visible on the dashboard
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Path inputFile = Paths.get(uploadDir, message.getStoredFileName());
        Path outputDirectory = Paths.get(processedDir, video.getId().toString());
        Files.createDirectories(outputDirectory);
        if (inputFile.getParent() != null) {
            Files.createDirectories(inputFile.getParent());
        }

        try {
            // Download original file from MinIO
            logger.info("Downloading original file {} from MinIO", message.getStoredFileName());
            try (InputStream in = minioService.getOriginalObject(message.getStoredFileName())) {
                Files.copy(in, inputFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // 1. Generate Renditions
            ffmpegService.generateHls(inputFile, outputDirectory);

            // 2. Generate Master Playlist
            masterPlaylistService.generateMasterPlaylist(outputDirectory);

            // 3. Generate Thumbnail
            ffmpegService.generateThumbnail(inputFile, outputDirectory);

            Path outputFile = outputDirectory.resolve("master.m3u8");
            if (!Files.exists(outputFile)) {
                throw new IOException("HLS master playlist file (master.m3u8) was not created.");
            }

            // 4. Extract Metadata
            logger.info("Extracting metadata for video {}", video.getId());
            VideoMetadata metadata = ffProbeService.extractMetadata(inputFile);

            // 5. Upload processed HLS files to MinIO
            logger.info("Uploading processed HLS files to MinIO for video {}", video.getId());
            try (var walkStream = Files.walk(outputDirectory)) {
                walkStream.filter(Files::isRegularFile).forEach(path -> {
                    String relativePath = outputDirectory.relativize(path).toString();
                    minioService.uploadProcessedFile(video.getId(), relativePath, path);
                });
            }

            // 6. Persist Metadata
            video.setDuration(metadata.getDuration());
            video.setWidth(metadata.getWidth());
            video.setHeight(metadata.getHeight());
            video.setVideoCodec(metadata.getVideoCodec());
            video.setAudioCodec(metadata.getAudioCodec());
            video.setBitRate(metadata.getBitRate());

            // 7. Save Video and Update status to COMPLETED
            video.setStatus(VideoStatus.COMPLETED);
            videoRepository.save(video);
            logger.info("Video {} status updated to COMPLETED with metadata", video.getId());
        } finally {
            // Clean up temporary local files
            Files.deleteIfExists(inputFile);
            if (Files.exists(outputDirectory)) {
                try (var walkStream = Files.walk(outputDirectory)) {
                    walkStream.sorted(java.util.Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException ignored) {}
                            });
                } catch (IOException ignored) {}
            }
        }
    }
}
