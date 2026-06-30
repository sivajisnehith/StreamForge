package com.Opsfusionn.StreamForge.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class VideoProcessingConsumer {
    private static final Logger logger =
        LoggerFactory.getLogger(VideoProcessingConsumer.class);
    @RabbitListener(queues = "video.processing")
    public void processVideo(VideoProcessingMessage message){
         logger.info("Video ID: {}", message.getVideoId());

        logger.info("Stored File: {}", message.getStoredFileName());
    }
}
