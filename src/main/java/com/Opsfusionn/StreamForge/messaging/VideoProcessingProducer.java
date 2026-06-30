package com.Opsfusionn.StreamForge.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class VideoProcessingProducer {
 private final RabbitTemplate rabbitTemplate;

    public VideoProcessingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(VideoProcessingMessage message) {
        rabbitTemplate.convertAndSend("video.processing", message);
    }
}