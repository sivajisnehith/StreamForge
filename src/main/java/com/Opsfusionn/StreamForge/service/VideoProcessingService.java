package com.Opsfusionn.StreamForge.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Opsfusionn.StreamForge.exception.VideoNotFoundException;
import com.Opsfusionn.StreamForge.messaging.VideoProcessingConsumer;
import com.Opsfusionn.StreamForge.messaging.VideoProcessingMessage;
import com.Opsfusionn.StreamForge.model.Video;
import com.Opsfusionn.StreamForge.model.VideoStatus;
import com.Opsfusionn.StreamForge.repository.VideoRepository;

@Service
public class VideoProcessingService {
    private final VideoRepository videoRepository;
    private static final Logger logger = LoggerFactory.getLogger(VideoProcessingConsumer.class);

    public VideoProcessingService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public void processVideo(VideoProcessingMessage message) {
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
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                System.getLogger(VideoProcessingService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }

            video.setStatus(VideoStatus.COMPLETED);
            videoRepository.save(video);

            logger.info("Video {} status updated to COMPLETED", video.getId());
    }

}
