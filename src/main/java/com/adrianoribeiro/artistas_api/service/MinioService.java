package com.adrianoribeiro.artistas_api.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadImagem(MultipartFile file) {
        try {
            String nomeArquivo = UUID.randomUUID() + "-" + file.getOriginalFilename();

            // Upload do arquivo
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(nomeArquivo)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // Gera URL temporária (c/ 15 minutos)
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(nomeArquivo)
                            .expiry(30, TimeUnit.MINUTES)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar imagem para o MinIO", e);
        }
    }
}
