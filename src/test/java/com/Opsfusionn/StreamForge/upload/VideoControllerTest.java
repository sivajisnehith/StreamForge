package com.Opsfusionn.StreamForge.upload;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
@AutoConfigureMockMvc(addFilters = false)
public class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private com.Opsfusionn.StreamForge.service.JwtService jwtService;

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

    @Test
    public void testDeleteVideo_Success() throws Exception {
        UUID validId = UUID.randomUUID();
        
        mockMvc.perform(delete("/api/videos/" + validId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteVideo_NotFound() throws Exception {
        UUID validId = UUID.randomUUID();
        
        org.mockito.Mockito.doThrow(new com.Opsfusionn.StreamForge.exception.VideoNotFoundException("Video not found."))
                .when(fileStorageService).deleteVideo(any(UUID.class));
                
        mockMvc.perform(delete("/api/videos/" + validId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Video not found."));
    }

    @Test
    public void testDeleteVideo_IOException() throws Exception {
        UUID validId = UUID.randomUUID();
        
        org.mockito.Mockito.doAnswer(invocation -> {
            throw new java.io.IOException("Failed to delete file.");
        }).when(fileStorageService).deleteVideo(any(UUID.class));
                
        mockMvc.perform(delete("/api/videos/" + validId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Failed to delete file."));
    }

    // Test 1 - No search
    @Test
    public void testGetAllVideos_NoSearch() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        VideoResponse v1 = new VideoResponse(id1, "v1.mp4", 100L, "video/mp4", VideoStatus.COMPLETED, LocalDateTime.now());
        v1.setTitle("Spring Boot Tutorial");
        VideoResponse v2 = new VideoResponse(id2, "v2.mp4", 200L, "video/mp4", VideoStatus.COMPLETED, LocalDateTime.now());
        v2.setTitle("Java Programming");

        org.springframework.data.domain.Page<VideoResponse> mockPage = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(v1, v2)
        );

        when(fileStorageService.getAllVideos(org.mockito.ArgumentMatchers.eq(null), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/videos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot Tutorial"))
                .andExpect(jsonPath("$.content[1].title").value("Java Programming"));
    }

    // Test 2 - Search by title
    @Test
    public void testGetAllVideos_SearchByTitle() throws Exception {
        UUID id = UUID.randomUUID();
        VideoResponse v = new VideoResponse(id, "v1.mp4", 100L, "video/mp4", VideoStatus.COMPLETED, LocalDateTime.now());
        v.setTitle("Spring Boot Tutorial");

        org.springframework.data.domain.Page<VideoResponse> mockPage = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(v)
        );

        when(fileStorageService.getAllVideos(org.mockito.ArgumentMatchers.eq("Spring"), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/videos").param("search", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot Tutorial"));
    }

    // Test 3 - Case insensitive search
    @Test
    public void testGetAllVideos_CaseInsensitiveSearch() throws Exception {
        UUID id = UUID.randomUUID();
        VideoResponse v = new VideoResponse(id, "v1.mp4", 100L, "video/mp4", VideoStatus.COMPLETED, LocalDateTime.now());
        v.setTitle("Spring Boot Tutorial");

        org.springframework.data.domain.Page<VideoResponse> mockPage = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(v)
        );

        when(fileStorageService.getAllVideos(org.mockito.ArgumentMatchers.eq("spring"), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/videos").param("search", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot Tutorial"));
    }

    // Test 4 - Partial search
    @Test
    public void testGetAllVideos_PartialSearch() throws Exception {
        UUID id = UUID.randomUUID();
        VideoResponse v = new VideoResponse(id, "v1.mp4", 100L, "video/mp4", VideoStatus.COMPLETED, LocalDateTime.now());
        v.setTitle("Spring Boot Tutorial");

        org.springframework.data.domain.Page<VideoResponse> mockPage = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(v)
        );

        when(fileStorageService.getAllVideos(org.mockito.ArgumentMatchers.eq("Boot"), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/videos").param("search", "Boot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot Tutorial"));
    }

    // Test 5 - Search + Pagination
    @Test
    public void testGetAllVideos_SearchAndPagination() throws Exception {
        UUID id = UUID.randomUUID();
        VideoResponse v = new VideoResponse(id, "v1.mp4", 100L, "video/mp4", VideoStatus.COMPLETED, LocalDateTime.now());
        v.setTitle("Spring Boot Tutorial");

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 1);
        org.springframework.data.domain.Page<VideoResponse> mockPage = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(v),
                pageable,
                5
        );

        when(fileStorageService.getAllVideos(org.mockito.ArgumentMatchers.eq("Spring"), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/videos")
                .param("search", "Spring")
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot Tutorial"))
                .andExpect(jsonPath("$.totalElements").value(5));
    }


    @Test
    public void testUpdateVideoStatus_Success() throws Exception {
        UUID validId = UUID.randomUUID();
        
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/videos/" + validId + "/status")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"status\": \"PROCESSING\"}"))
                .andExpect(status().isNoContent());
    }
}
