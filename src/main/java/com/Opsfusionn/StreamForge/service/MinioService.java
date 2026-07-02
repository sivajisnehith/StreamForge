 package com.Opsfusionn.StreamForge.service;
    
    import java.nio.file.Path;

    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;

    import io.minio.MinioClient;
    import io.minio.UploadObjectArgs;

@Service
public class MinioService {
    private final MinioClient minioClient;

    @Value("${minio.original-bucket}")
    private String originalBucket;

    @Value("${minio.processed-bucket}")
    private String processedBucket;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }
    
    public void uploadOriginalFile(String objectName, Path filePath) {

        try {

            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(originalBucket)
                            .object(objectName)
                            .filename(filePath.toString())
                            .build());

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO.", e);
        }
    }

    public void downloadOriginalFile(String objectName,Path destination){

    }

}
