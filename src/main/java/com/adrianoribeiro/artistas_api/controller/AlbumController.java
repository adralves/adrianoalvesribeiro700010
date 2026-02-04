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
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/album")
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
            summary = "Listar álbuns por tipo de artista(CANTOR/BANDA) ",
            description = """ 
                     Filtra a listagem de álbuns por tipo de artista. Permite identificar
                     se o álbum pertence a um cantor, ou uma banda.
                     """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de álbuns retornada com sucesso")
    })
    @GetMapping("/tipo-artista")
    public ResponseEntity<Page<Album>> listarAlbunsPorTipoArtista(@RequestParam(required = false) TipoArtista tipo,
                                            @ParameterObject Pageable pageable) {

        Page<Album> page = albumService.listarAlbunsPorTipoArtista(tipo, pageable);

        return ResponseEntity.ok(page);
    }


    @Operation(
            summary = "Consulta álbum pelo nome do artista",
            description =  """
                Permite consultar álbuns filtrando pelo nome do artista (parcial ou completo),
                com ordenação.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de álbuns retornada com sucesso")
    })
    @GetMapping("/album-por-artista")
    public ResponseEntity<Page<Album>> listaAlbunsPorArtista(@RequestParam(required = false) String artista,
                                             @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(albumService.listaAlbunsPorArtista(artista, pageable));


    }

    @Operation(
            summary = "Buscar álbum por ID",
            description = "Retorna os dados de um álbum a partir do seu identificador"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Álbum encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Álbum não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public Album buscarAlbumPorId(@PathVariable Long id) {
        return albumService.buscarPorId(id);
    }

    @Operation(
            summary = "Excluir álbum",
            description = "Remove um álbum do sistema a partir do seu ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Álbum removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Álbum não encontrado", content = @Content)
    })

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirAlbum(@PathVariable Long id) {
        albumService.excluir(id);
    }

    @Operation(
            summary = "Listar álbuns do artista",
            description = "Retorna todos os álbuns associados a um artista"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de álbuns retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado", content = @Content)
    })
    @GetMapping("/artista/{id}")
    public ResponseEntity<List<Album>> listarAlbunsDoArtista(@PathVariable Long id) {
        return ResponseEntity.ok(albumService.listarAlbunsDoArtista(id));
    }
}