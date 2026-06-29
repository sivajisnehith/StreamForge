    package com.Opsfusionn.StreamForge.service;
    import java.io.File;
    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Optional;
    import java.util.Set;
    import java.util.UUID;

    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.web.multipart.MultipartFile;

import com.Opsfusionn.StreamForge.dto.UpdateVideoStatusRequest;
import com.Opsfusionn.StreamForge.dto.VideoResponse;
    import com.Opsfusionn.StreamForge.exception.VideoNotFoundException;
    import com.Opsfusionn.StreamForge.model.Video;
    import com.Opsfusionn.StreamForge.model.VideoStatus;
    import com.Opsfusionn.StreamForge.repository.VideoRepository;

    
    @Service
    public class FileStorageService {
        private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
        private final VideoRepository videoRepository;

        public FileStorageService(VideoRepository videoRepository) {
            this.videoRepository = videoRepository;
        }

        @Value("${streamforge.storage.max-file-size}")
        private long MAX_FILE_SIZE;

        private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "video/mp4",
            "video/x-matroska",
            "video/quicktime"
        );  

        private static final Set<String> ALLOWED_EXTENSIONS =
        Set.of(".mp4", ".mkv", ".mov");


        @Value("${streamforge.storage.upload-dir}")
        private String uploadDir;


        public Video storeFile(MultipartFile file) throws IOException{
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Uploaded file is empty.");
            }
            
            String contentType = file.getContentType(); //Gives us the MIME type like image/png
            
            if(contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)){
                throw new IllegalArgumentException("Only video files are allowed.");
            }

            //names of content type and file size
            logger.info("Content type: {}", contentType);
            logger.info("File size: {} bytes", file.getSize());
            File directory = new File(uploadDir);

            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    throw new IOException("Could not create upload directory.");
                }
            }

            //original filename
            String originalFileName = file.getOriginalFilename();
            if(originalFileName == null){
                throw new IllegalArgumentException("Original file name is missing");
            }
            if (originalFileName.isBlank()) {
                throw new IllegalArgumentException("Filename cannot be blank.");
            }


            int dotIndex = originalFileName.lastIndexOf(".");
            if (dotIndex == -1) {
                throw new IllegalArgumentException("File has no extension.");
            }
            if (dotIndex == originalFileName.length() - 1) {
                throw new IllegalArgumentException("File extension is missing.");
            }

            
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("File exceeds maximum allowed size.");
            }

            
            String extension = originalFileName.substring(dotIndex);
            if(!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())){
                throw new IllegalArgumentException(
                        "Unsupported extension.");
            }
            //video id
            UUID videoId = UUID.randomUUID();

            //storedfilename
            String storedFileName = videoId.toString() + extension;
            logger.info("Original file: {}", originalFileName);
            logger.info("Stored file: {}", storedFileName);
            
            //we are using paths.get to basically use the correct / because of different os
            Path filePath = Paths.get(uploadDir, storedFileName);
            try {
                Files.write(filePath, file.getBytes());
            }
            catch(IOException ex){
                throw new IOException(
                        "Failed to save file.",
                        ex
                );
            }

            Video video = new Video();
            video.setId(videoId);
            video.setOriginalFileName(originalFileName);
            video.setStoredFileName(storedFileName);
            video.setFileSize(file.getSize());
            video.setContentType(contentType);
            video.setStatus(VideoStatus.UPLOADED);

            

            videoRepository.save(video);
            return video;
        }

        //private helper method to actualy map to videos
        private VideoResponse mapToVideoResponse(Video video){
            VideoResponse response = new VideoResponse();

            response.setVideoId(video.getId());
            response.setOriginalFileName(video.getOriginalFileName());
            response.setFileSize(video.getFileSize());
            response.setContentType(video.getContentType());
            response.setStatus(video.getStatus());
            response.setUploadedAt(video.getUploadedAt());

            return response;
        }


        //This is for (GET "/api/vidoes/{id}")
        public VideoResponse getVideoById(UUID videoId){
            Optional<Video> videoOptional = videoRepository.findById(videoId);
            if (videoOptional.isEmpty()) {
                throw new VideoNotFoundException("Video not found.");
            }

            Video video = videoOptional.get();
            return mapToVideoResponse(video);   
        }

        //This is for (GET "/api/videos")
        public List<VideoResponse> getAllVideos(){
            List<Video> videos = videoRepository.findAll();
            List<VideoResponse> responses = new ArrayList<>();
            for(Video video:videos){
                responses.add(mapToVideoResponse(video));
            }
            return responses;
        }


        //This is for (DELETE "/api/videos/{id}")
        public void deleteVideo(UUID videoId) throws IOException {
            Optional<Video> videoOptional = videoRepository.findById(videoId);

            if (videoOptional.isEmpty()) {
                throw new VideoNotFoundException("Video not found.");
            }

            //reconstructing the videopath
            Video video = videoOptional.get();
            Path filePath = Paths.get(uploadDir, video.getStoredFileName());
            Files.deleteIfExists(filePath);
            videoRepository.delete(video);
        }

        //Method to update the status
        public void updateVideoStatus(UUID videoId,UpdateVideoStatusRequest request) {
            Optional<Video> videoOptional = videoRepository.findById(videoId);

            if (videoOptional.isEmpty()) {
                throw new VideoNotFoundException("Video not found.");
            }

            Video video = videoOptional.get();

            //check for state machine architecture
            if (!video.getStatus().canTransitionTo(request.getStatus())) {
                throw new IllegalStateException(
                    "Cannot change video status from " +
                    video.getStatus() +
                    " to " +
                    request.getStatus()
                );
            }

            video.setStatus(request.getStatus());

            videoRepository.save(video);
        }

    }
