package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.dto.AtualizarAlbumDTO;
import com.adrianoribeiro.artistas_api.dto.CriarAlbumDTO;
import com.adrianoribeiro.artistas_api.model.enums.TipoArtista;
import com.adrianoribeiro.artistas_api.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/albuns")
@Tag(name = "Álbuns", description = "Endpoints para gerenciamento de álbuns")
@SecurityRequirement(name = "bearerAuth")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @Operation(
            summary = "Criar um novo álbum",
            description = "Cria um álbum informando os dados básicos"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Álbum criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Album> criarAlbum(@Valid @RequestBody CriarAlbumDTO dto) {
        Album album = new Album();
        album.setNome(dto.getNome());
        return ResponseEntity.ok(albumService.criarAlbum(album));
    }

    @Operation(
            summary = "Listar álbuns",
            description = "Lista álbuns com paginação, podendo filtrar pelo nome"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de álbuns retornada com sucesso")
    })
    @GetMapping
    public Page<Album> listarAlbuns(@RequestParam(required = false) String nome,
                                    @ParameterObject Pageable pageable){
        return albumService.listarAlbuns(nome, pageable);
    }

    @Operation(
            summary = "Atualizar álbum",
            description = "Atualiza os dados de um álbum pelo ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Álbum atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Álbum não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Album> atualizarAlbum(@PathVariable Long id,@Valid @RequestBody AtualizarAlbumDTO dto) {
        return ResponseEntity.ok(albumService.atualizarAlbum(id, dto));
    }

    @Operation(
            summary = "Listar álbuns por tipo de artista",
            description = "Lista álbuns filtrando pelo tipo do artista (CANTOR ou BANDA) paginado com ordenação asc/desc"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de álbuns retornada com sucesso")
    })
    @GetMapping("/tipo-artista")
    public Page<Album> listarAlbunsPorTipoArtista(@RequestParam TipoArtista tipo,
                                                  @ParameterObject Pageable pageable) {
        return albumService.listarPorTipoArtista(tipo, pageable);
    }

    @Operation(
            summary = "Listar álbum pelo nome do artista",
            description = "Lista álbuns filtrando pelo nome do artista"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de álbuns retornada com sucesso")
    })
    @GetMapping("/album-por-artista")
    public Page<Album> listaAlbunsPorArtista(@RequestParam(required = false) String artista,
                                             @ParameterObject Pageable pageable) {
        return albumService.listaAlbunsPorArtista(artista, pageable);
    }
}