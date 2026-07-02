package com.Opsfusionn.StreamForge.messaging;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.Opsfusionn.StreamForge.model.Video;
import com.Opsfusionn.StreamForge.model.VideoStatus;
import com.Opsfusionn.StreamForge.repository.VideoRepository;

@Component("videoProcessingErrorHandler")
public class VideoProcessingErrorHandler implements RabbitListenerErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(VideoProcessingErrorHandler.class);
    private final VideoRepository videoRepository;

    public VideoProcessingErrorHandler(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Override
    public Object handleError(org.springframework.amqp.core.Message amqpMessage, 
                              com.rabbitmq.client.Channel channel,
                              Message<?> message, 
                              ListenerExecutionFailedException exception) throws Exception {
        Object payload = message.getPayload();
        if (payload instanceof VideoProcessingMessage) {
            VideoProcessingMessage videoMessage = (VideoProcessingMessage) payload;
            UUID videoId = videoMessage.getVideoId();
            logger.error("Global listener error handler caught exception for video ID {}: {}", videoId, exception.getMessage());
            videoRepository.findById(videoId).ifPresent(video -> {
                video.setStatus(VideoStatus.FAILED);
                videoRepository.save(video);
                logger.info("Video {} status updated to FAILED by global error handler", videoId);
            });
        } else {
            logger.error("Global listener error handler caught exception, payload is not VideoProcessingMessage: {}", exception.getMessage());
        }
        throw exception;
    }
}
