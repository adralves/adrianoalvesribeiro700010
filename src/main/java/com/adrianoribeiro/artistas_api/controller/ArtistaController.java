package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.dto.AtualizarArtistaDTO;
import com.adrianoribeiro.artistas_api.dto.CriarArtistaDTO;
import com.adrianoribeiro.artistas_api.service.ArtistaService;
import com.adrianoribeiro.artistas_api.model.Artista;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/artistas")
@Tag(name = "Artistas", description = "Endpoints para gerenciamento de artistas")
@SecurityRequirement(name = "bearerAuth")
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
            summary = "Listar artistas",
            description = "Retorna uma lista de artistas paginada. Permite busca parcial ou completa pelo nome e ordenação alfabética."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de artistas retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<Page<Artista>> listarArtista(@RequestParam(required = false) String nome,
                                       @ParameterObject
                                       @PageableDefault(sort = "nome", direction = Sort.Direction.ASC)Pageable pageable){
        return ResponseEntity.ok(artistaService.listarArtista(nome, pageable));
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
    public ResponseEntity<Artista> atualizarArtista(@PathVariable Long id,@Valid @RequestBody AtualizarArtistaDTO dto) {
        return ResponseEntity.ok(artistaService.atualizarArtista(id, dto));
    }

    @Operation(
            summary = "Excluir artista",
            description = "Remove um artista do sistema pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Artista excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito: Artista possui álbuns vinculados")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        artistaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
