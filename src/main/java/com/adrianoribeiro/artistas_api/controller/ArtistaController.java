package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.dto.AtualizarArtistaDTO;
import com.adrianoribeiro.artistas_api.dto.CriarArtistaDTO;
import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.service.ArtistaService;
import com.adrianoribeiro.artistas_api.model.Artista;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/artistas")
@Tag(name = "Artistas", description = "Endpoints para gerenciamento de artistas")
public class ArtistaController {

    private final ArtistaService artistaService;

    public ArtistaController(ArtistaService artistaService) {
        this.artistaService = artistaService;
    }

    @PostMapping
    @Operation(
            summary = "Criar um novo artista",
            description = "Cria um artista informando nome e tipo(CANTOR ou BANDA)"
    )
    public ResponseEntity<Artista> criarArtista(@Valid @RequestBody CriarArtistaDTO dto) {
        Artista artista = new Artista();
        artista.setNome(dto.getNome());
        artista.setTipo(dto.getTipo());
        return ResponseEntity.ok(artistaService.criarArtista(artista));
    }


    @Operation(
            summary = "Listar artistas pelo nome",
            description = "Lista artistas com paginação, podendo filtrar pelo nome(não obrigatorio) e com ordenação alfabetica pelo nome(asc/desc) ou id(asc/desc) "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de artistas retornada com sucesso")
    })
    @GetMapping
    public Page<Artista> listarArtista(@RequestParam(required = false) String nome,
                                       @ParameterObject Pageable pageable){
        return artistaService.listarArtista(nome, pageable);
    }

    @Operation(
            summary = "Atualizar artista",
            description = "Atualiza os dados de um artista pelo ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artista atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Artista> atualizarAlbum(@PathVariable Long id,@Valid @RequestBody AtualizarArtistaDTO dto) {
        return ResponseEntity.ok(artistaService.atualizarArtista(id, dto));
    }


    @Operation(
            summary = "Listar álbuns do artista",
            description = "Retorna todos os álbuns associados a um artista"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de álbuns retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado", content = @Content)
    })
    @GetMapping("/{id}/albuns")
    public ResponseEntity<List<Album>> listarAlbuns(@PathVariable Long id) {
        return ResponseEntity.ok(artistaService.listarAlbunsArtista(id));
    }

}
