/*
package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.dto.UploadImagemResponseDTO;
import com.adrianoribeiro.artistas_api.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/imagens")
public class ImagemController {

    private final MinioService minioService;

    public ImagemController(MinioService minioService) {
        this.minioService = minioService;
    }

    @Operation(
            summary = "Upload de imagem"
    )    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UploadImagemResponseDTO> upload(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new UploadImagemResponseDTO("Arquivo vazio"));
        }

        String url = minioService.uploadImagem(file);
        return ResponseEntity.ok(new UploadImagemResponseDTO(url));
    }
}
*/
