package com.Opsfusionn.StreamForge.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.Opsfusionn.StreamForge.service.VideoProcessingService;

@Component
public class VideoProcessingConsumer {
    private static final Logger logger = LoggerFactory.getLogger(VideoProcessingConsumer.class);
    
    //Video Processing service configuration
    private final VideoProcessingService videoProcessingService;

    public VideoProcessingConsumer(VideoProcessingService videoProcessingService) {
        this.videoProcessingService = videoProcessingService;
    }

    @RabbitListener(queues = "video.processing")
    public void processVideo(VideoProcessingMessage message){
         videoProcessingService.processVideo(message);
    }
}
