package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.dto.UploadAlbumImagensResponseDTO;
import com.adrianoribeiro.artistas_api.service.AlbumImagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/albuns")
@Tag(
        name = "Álbum x Imagem",
        description = "Endpoints para gerenciar imagens"
)
@SecurityRequirement(name = "bearerAuth")
public class AlbumImagemController {

    private final AlbumImagemService albumImagemService;

    public AlbumImagemController(AlbumImagemService albumImagemService) {
        this.albumImagemService = albumImagemService;
    }

    @PostMapping(
            value = "/{albumId}/capas",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Upload de uma ou mais imagens de capa do álbum")
    public ResponseEntity<UploadAlbumImagensResponseDTO> uploadCapas(
            @PathVariable Long albumId,

            @Parameter(
                    description = "Imagens de capa do álbum",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            array = @ArraySchema(
                                    schema = @Schema(type = "string", format = "binary")
                            )
                    )
            )
            @RequestPart("files") MultipartFile[] files
    ) {

        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().build();
        }

        List<String> urls = albumImagemService.uploadCapas(albumId, files);
        return ResponseEntity.ok(new UploadAlbumImagensResponseDTO(urls));
    }
}
