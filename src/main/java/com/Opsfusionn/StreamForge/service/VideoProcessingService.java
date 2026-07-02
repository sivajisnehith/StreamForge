package com.Opsfusionn.StreamForge.service;

import java.io.IOException;
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
    
    private static final Logger logger = LoggerFactory.getLogger(VideoProcessingService.class);

    @Value("${streamforge.storage.upload-dir}")
    private String uploadDir;

    @Value("${streamforge.storage.processed-dir}")
    private String processedDir;

    public VideoProcessingService(VideoRepository videoRepository, 
                                  FFmpegService ffmpegService, 
                                  FFProbeService ffProbeService,
                                  MasterPlaylistService masterPlaylistService) {
        this.videoRepository = videoRepository;
        this.ffmpegService = ffmpegService;
        this.ffProbeService = ffProbeService;
        this.masterPlaylistService = masterPlaylistService;
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

        video.setStatus(VideoStatus.PROCESSING);
        videoRepository.save(video);
        
        logger.info("Video {} status updated to PROCESSING", video.getId());
        Path inputFile = Paths.get(uploadDir, message.getStoredFileName());
        Path outputDirectory = Paths.get(processedDir, video.getId().toString());
        Files.createDirectories(outputDirectory);
        
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

        // 5. Persist Metadata
        video.setDuration(metadata.getDuration());
        video.setWidth(metadata.getWidth());
        video.setHeight(metadata.getHeight());
        video.setVideoCodec(metadata.getVideoCodec());
        video.setAudioCodec(metadata.getAudioCodec());
        video.setBitRate(metadata.getBitRate());

        // 6. Save Video and Update status to COMPLETED
        video.setStatus(VideoStatus.COMPLETED);
        videoRepository.save(video);
        logger.info("Video {} status updated to COMPLETED with metadata", video.getId());
    }
}
