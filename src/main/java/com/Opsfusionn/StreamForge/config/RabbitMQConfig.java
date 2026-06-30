package com.Opsfusionn.StreamForge.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue videoProcessingQueue() {
        return new Queue("video.processing", true); //This is the actual queue inside and true makes the queue durable.
    }

    
}