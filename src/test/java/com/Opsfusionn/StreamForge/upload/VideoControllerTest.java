package com.Opsfusionn.StreamForge.upload;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.Opsfusionn.StreamForge.controller.VideoController;
import com.Opsfusionn.StreamForge.dto.VideoResponse;
import com.Opsfusionn.StreamForge.exception.GlobalExceptionHandler;
import com.Opsfusionn.StreamForge.model.VideoStatus;
import com.Opsfusionn.StreamForge.service.FileStorageService;

@WebMvcTest(VideoController.class)
@Import(GlobalExceptionHandler.class)
public class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    public void testGetVideoById_Success() throws Exception {
        UUID validId = UUID.randomUUID();
        VideoResponse mockResponse = new VideoResponse(
                validId,
                "test_video.mp4",
                1024L,
                "video/mp4",
                VideoStatus.PENDING,
                LocalDateTime.now()
        );

        when(fileStorageService.getVideoById(any(UUID.class))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/videos/" + validId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videoId").value(validId.toString()))
                .andExpect(jsonPath("$.originalFileName").value("test_video.mp4"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    public void testGetVideoById_InvalidFormat() throws Exception {
        mockMvc.perform(get("/api/videos/invalid-uuid-format"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid video ID format."));
    }
}
