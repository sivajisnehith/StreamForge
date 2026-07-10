package com.Opsfusionn.StreamForge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;

@Configuration
public class MinioConfig {

    @Value("${minio.url}")
    private String url;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.original-bucket}")
    private String originalBucket;

    @Value("${minio.processed-bucket}")
    private String processedBucket;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();

        // Auto-create original and processed buckets on startup if they don't exist
        try {
            boolean originalExists = client.bucketExists(
                BucketExistsArgs.builder().bucket(originalBucket).build()
            );
            if (!originalExists) {
                client.makeBucket(
                    MakeBucketArgs.builder().bucket(originalBucket).build()
                );
                System.out.println("Created MinIO bucket: " + originalBucket);
            }

            boolean processedExists = client.bucketExists(
                BucketExistsArgs.builder().bucket(processedBucket).build()
            );
            if (!processedExists) {
                client.makeBucket(
                    MakeBucketArgs.builder().bucket(processedBucket).build()
                );
                System.out.println("Created MinIO bucket: " + processedBucket);
            }
        } catch (Exception e) {
            System.err.println("Failed to auto-create MinIO buckets: " + e.getMessage());
            e.printStackTrace();
        }

        return client;
    }
}