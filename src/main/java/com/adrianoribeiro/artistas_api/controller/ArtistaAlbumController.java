package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.dto.VinculoArtistaAlbumDTO;
import com.adrianoribeiro.artistas_api.service.ArtistaAlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/artistas-albuns")
@Tag(
        name = "Artistas x Álbuns",
        description = "Endpoints para vínculo entre artistas e álbuns"
)
public class ArtistaAlbumController {

    private final ArtistaAlbumService artistaAlbumService;

    public ArtistaAlbumController(ArtistaAlbumService artistaAlbumService) {
        this.artistaAlbumService = artistaAlbumService;
    }

    @Operation(
            summary = "Vincular artista a álbum",
            description = "Cria o vínculo entre um artista e um álbum existentes"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vínculo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Artista ou álbum não encontrado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Album> vincularArtistaAlbum(@Valid @RequestBody VinculoArtistaAlbumDTO dto) {
        return ResponseEntity.ok(
                artistaAlbumService.vincularArtistaAlbum(
                        dto.getArtistaId(),
                        dto.getAlbumId()
                )
        );
    }}
