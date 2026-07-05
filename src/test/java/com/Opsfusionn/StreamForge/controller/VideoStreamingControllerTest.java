package com.Opsfusionn.StreamForge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.Opsfusionn.StreamForge.exception.GlobalExceptionHandler;
import com.Opsfusionn.StreamForge.exception.VideoNotFoundException;
import com.Opsfusionn.StreamForge.service.MinioService;

@WebMvcTest(VideoStreamingController.class)
@Import(GlobalExceptionHandler.class)
public class VideoStreamingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MinioService minioService;

    @Test
    public void testStreamMasterPlaylist_Success() throws Exception {
        UUID videoId = UUID.randomUUID();
        byte[] contentBytes = "#EXTM3U\n#EXT-X-VERSION:3".getBytes();
        ByteArrayInputStream mockInputStream = new ByteArrayInputStream(contentBytes);

        when(minioService.getProcessedObject(eq(videoId), eq("master.m3u8"))).thenReturn(mockInputStream);

        mockMvc.perform(get("/api/videos/" + videoId + "/master.m3u8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.apple.mpegurl"))
                .andExpect(content().bytes(contentBytes));
    }

    @Test
    public void testStreamVideoFile_Success() throws Exception {
        UUID videoId = UUID.randomUUID();
        byte[] contentBytes = new byte[]{1, 2, 3, 4};
        ByteArrayInputStream mockInputStream = new ByteArrayInputStream(contentBytes);

        when(minioService.getProcessedObject(eq(videoId), eq("720p.m3u8"))).thenReturn(mockInputStream);

        mockMvc.perform(get("/api/videos/" + videoId + "/720p.m3u8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.apple.mpegurl"))
                .andExpect(content().bytes(contentBytes));
    }

    @Test
    public void testStreamSegmentFile_Success() throws Exception {
        UUID videoId = UUID.randomUUID();
        byte[] contentBytes = new byte[]{5, 6, 7, 8};
        ByteArrayInputStream mockInputStream = new ByteArrayInputStream(contentBytes);

        when(minioService.getProcessedObject(eq(videoId), eq("1080/segment000.ts"))).thenReturn(mockInputStream);

        mockMvc.perform(get("/api/videos/" + videoId + "/1080/segment000.ts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("video/mp2t"))
                .andExpect(content().bytes(contentBytes));
    }

    @Test
    public void testStreamVideo_NotFound() throws Exception {
        UUID videoId = UUID.randomUUID();

        when(minioService.getProcessedObject(eq(videoId), eq("nonexistent.m3u8")))
                .thenThrow(new VideoNotFoundException("Processed video file or playlist not found: nonexistent.m3u8"));

        mockMvc.perform(get("/api/videos/" + videoId + "/nonexistent.m3u8"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Processed video file or playlist not found: nonexistent.m3u8"));
    }

    @Test
    public void testStreamVideo_InvalidUUIDFormat() throws Exception {
        mockMvc.perform(get("/api/videos/invalid-uuid-format/master.m3u8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid video ID format."));
    }
}
