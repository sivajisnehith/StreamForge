package com.Opsfusionn.StreamForge.messaging;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.messaging.support.MessageBuilder;

import com.Opsfusionn.StreamForge.model.Video;
import com.Opsfusionn.StreamForge.model.VideoStatus;
import com.Opsfusionn.StreamForge.repository.VideoRepository;
import com.rabbitmq.client.Channel;

@ExtendWith(MockitoExtension.class)
public class VideoProcessingErrorHandlerTest {

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoProcessingErrorHandler errorHandler;

    @Test
    public void testHandleError_UpdatesVideoToFailed() throws Exception {
        UUID videoId = UUID.randomUUID();
        VideoProcessingMessage payload = new VideoProcessingMessage(videoId, "test.mp4");
        org.springframework.messaging.Message<VideoProcessingMessage> message = MessageBuilder.withPayload(payload).build();
        
        Video video = new Video();
        video.setId(videoId);
        video.setStatus(VideoStatus.PROCESSING);

        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));

        Message amqpMessage = mock(Message.class);
        Channel channel = mock(Channel.class);
        ListenerExecutionFailedException exception = new ListenerExecutionFailedException("Processing failed", new RuntimeException());

        assertThrows(ListenerExecutionFailedException.class, () -> {
            errorHandler.handleError(amqpMessage, channel, message, exception);
        });

        assertEquals(VideoStatus.FAILED, video.getStatus());
        verify(videoRepository, times(1)).save(video);
    }
}
