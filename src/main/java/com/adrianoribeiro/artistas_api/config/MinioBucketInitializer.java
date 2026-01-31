package com.adrianoribeiro.artistas_api.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioBucketInitializer {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucketName;

    private MinioClient minioClient;

    @PostConstruct
    public void createBucketIfNotExists() {
        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(minioUrl)
                    .credentials(accessKey, secretKey)
                    .build();

            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                System.out.println("Bucket criado com sucesso: " + bucketName);
            } else {
                System.out.println("Bucket já existe: " + bucketName);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao inicializar bucket no MinIO", e);
        }
    }
}