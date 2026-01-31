package com.adrianoribeiro.artistas_api.health;

import io.minio.MinioClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MinioHealthIndicator implements HealthIndicator {

    private final MinioClient minioClient;

    public MinioHealthIndicator(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public Health health() {
        try {
            minioClient.listBuckets();

            return Health.up()
                    .withDetail("minio", "Disponível")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("minio", "Indisponível")
                    .withException(e)
                    .build();
        }
    }
}