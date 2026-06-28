    package com.Opsfusionn.StreamForge.service;

    import java.io.File;
    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.util.Set;
    import java.util.UUID;

    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.web.multipart.MultipartFile;


    @Service
    public class FileStorageService {
        private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

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


        public String storeFile(MultipartFile file) throws IOException{
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

            //storedfilename
            String storedFileName = UUID.randomUUID().toString() + extension;
            logger.info("Original file: {}", originalFileName);
            logger.info("Stored file: {}", storedFileName);
            
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
            return storedFileName;
        }
    }
