package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.dto.AlbumImagemResponseDTO;
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
import java.util.Map;

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
            value = "/{albumId}/imagens",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Upload de uma ou mais imagens de capa do álbum")
    public ResponseEntity<UploadAlbumImagensResponseDTO> adicionarImagens(
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

        List<String> urls = albumImagemService.adicionarImagens(albumId, files);
        return ResponseEntity.ok(new UploadAlbumImagensResponseDTO(urls));
    }

    @GetMapping("/{albumId}")
    @Operation(summary = "Listar todas as imagens de um álbum")
    public ResponseEntity<List<AlbumImagemResponseDTO>> listar(@PathVariable Long albumId) {
        return ResponseEntity.ok(albumImagemService.listarImagens(albumId));
    }

    @DeleteMapping("/{imagemId}")
    @Operation(summary = "Remover uma imagem específica")
    public ResponseEntity<Void> excluir(@PathVariable Long imagemId) {
        albumImagemService.excluirImagem(imagemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{imagemId}/download")
    @Operation(summary = "Gera um link temporário para download")
    public ResponseEntity<Map<String, String>> download(@PathVariable Long imagemId) {
        String url = albumImagemService.download(imagemId);

        // Retornamos um JSON com a URL para facilitar o consumo
        return ResponseEntity.ok(Map.of("downloadUrl", url));
    }

    @PutMapping(
            value = "/{imagemId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Atualiza uma imagem específica substituindo o arquivo")
    public ResponseEntity<Map<String, String>> atualizarImagem(
            @PathVariable Long imagemId,
            @RequestPart("file") MultipartFile file) {

        String novaUrl = albumImagemService.atualizarImagem(imagemId, file);

        return ResponseEntity.ok(Map.of("url", novaUrl));
    }
}
