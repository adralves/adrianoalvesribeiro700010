package com.adrianoribeiro.artistas_api.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
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

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(nomeArquivo) // O MinIO guarda por esse nome
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return nomeArquivo;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar imagem", e);
        }
    }

    public void removerArquivo(String nomeArquivo) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucket).object(nomeArquivo).build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover imagem do MinIO", e);
        }
    }

    public String gerarUrl(String nomeArquivo) {
        try {
            String urlAssinada = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(nomeArquivo)
                            .expiry(30, TimeUnit.MINUTES)
                            .build());

            // replace para o Nginx fazer apontamento da url corretamente
            return urlAssinada.replace("http://minio:9000/", "http://localhost/minio/");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar URL do MinIO", e);
        }
    }

    public String download(String nomeArquivo) {
        try {
            // parâmetros que dizem ao navegador para baixar o arquivo
            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("response-content-disposition", "attachment; filename=\"" + nomeArquivo + "\"");

            String urlAssinada = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(nomeArquivo)
                            .expiry(10, TimeUnit.MINUTES) // Link curto para download imediato
                            .extraQueryParams(reqParams)
                            .build());

            // Mantendo seu padrão de proxy para o Nginx
            return urlAssinada.replace("http://minio:9000/", "http://localhost/minio/");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar link de download no MinIO", e);
        }
    }
}



