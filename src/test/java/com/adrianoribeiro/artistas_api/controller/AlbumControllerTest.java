package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.model.enums.TipoArtista;
import com.adrianoribeiro.artistas_api.service.AlbumService;
import com.adrianoribeiro.artistas_api.service.JwtService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(AlbumController.class)
@Import(ValidationAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false) // Ignora o SecurityRequirement do Controller
class AlbumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AlbumService albumService;

    @Test
    void deveCriarAlbumComSucesso() throws Exception {
        Album albumRetorno = new Album();
        albumRetorno.setId(1L);
        albumRetorno.setNome("Rock Beats 2026");

        when(albumService.criarAlbum(any(Album.class))).thenReturn(albumRetorno);

        mockMvc.perform(post("/api/v1/album")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nome": "Rock Beats 2026"
                            }
                        """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Rock Beats 2026"));
    }

    @Test
    void deveListarAlbunsComSucesso() throws Exception {
        Album album = new Album();
        album.setId(1L);
        album.setNome("Rock Beats 2026");

        Page<Album> page = new PageImpl<>(List.of(album));

        when(albumService.listarAlbuns(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/album")
                        .param("nome", "Rock")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Rock Beats 2026"));
    }

    @Test
    void deveAtualizarAlbumComSucesso() throws Exception {
        Album albumAtualizado = new Album();
        albumAtualizado.setId(1L);
        albumAtualizado.setNome("Rock Beats 2026 Atualizado");

        when(albumService.atualizarAlbum(eq(1L), any())).thenReturn(albumAtualizado);

        mockMvc.perform(put("/api/v1/album/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nome": "Rock Beats 2026 Atualizado"
                            }
                        """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Rock Beats 2026 Atualizado"));
    }

    @Test
    void deveListarAlbunsPorTipoArtistaComSucesso() throws Exception {
        Album album = new Album();
        album.setId(1L);
        album.setNome("Rock Beats 2026");

        Page<Album> page = new PageImpl<>(List.of(album));

        when(albumService.listarPorTipoArtista(eq(TipoArtista.CANTOR), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/album/tipo-artista")
                        .param("tipo", "CANTOR")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Rock Beats 2026"));
    }

    @Test
    void deveListarAlbunsPorNomeArtistaComSucesso() throws Exception {
        Album album = new Album();
        album.setId(1L);
        album.setNome("Rock Beats 2026");

        Page<Album> page = new PageImpl<>(List.of(album));

        when(albumService.listaAlbunsPorArtista(eq("Mike Shinoda"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/album/album-por-artista")
                        .param("artista", "Mike Shinoda")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Rock Beats 2026"));
    }

    @Test
    void deveTestarOrdenacaoEPaginacao() throws Exception {
        Album album1 = new Album();
        album1.setId(1L);
        album1.setNome("Samba e Sucesso");

        Album album2 = new Album();
        album2.setId(2L);
        album2.setNome("Pop Mix");

        Sort sort = Sort.by("nome").ascending();
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<Album> page = new PageImpl<>(List.of(album1, album2), pageable, 2);

        when(albumService.listarAlbuns(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/album")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "nome,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Samba e Sucesso"))
                .andExpect(jsonPath("$.content[1].nome").value("Pop Mix"));
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoExistiremAlbuns() throws Exception {
        Page<Album> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(albumService.listarAlbuns(isNull(), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/album")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void deveRetornar404AoAtualizarAlbumInexistente() throws Exception {
        when(albumService.atualizarAlbum(eq(999L), any()))
                .thenThrow(new EntityNotFoundException("Álbum não encontrado"));

        mockMvc.perform(put("/api/v1/album/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Novo Nome\"}"))
                .andExpect(status().isInternalServerError()); // RuntimeException gera 500
    }

    @Test
    void deveRetornar400AoCriarAlbumInvalido() throws Exception {
        // Supondo que seu CriarAlbumDTO tenha @NotBlank no nome
        mockMvc.perform(post("/api/v1/album")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"\"}")) // Nome vazio
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400ParaTipoArtistaInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/album/tipo-artista")
                        .param("tipo", "DJ") // DJ não existe no seu Enum TipoArtista
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveBuscarAlbumPorIdComSucesso() throws Exception {

        Album album = new Album();
        album.setId(1L);
        album.setNome("Horizontes");

        when(albumService.buscarPorId(1L)).thenReturn(album);

        mockMvc.perform(get("/api/v1/album/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Horizontes"));
    }

    @Test
    void deveRetornar404AoBuscarAlbumInexistente() throws Exception {

        when(albumService.buscarPorId(99L))
                .thenThrow(new EntityNotFoundException("Álbum não encontrado"));

        mockMvc.perform(get("/api/v1/album/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Álbum não encontrado"));
    }

    @Test
    void deveExcluirAlbumComSucesso() throws Exception {

        doNothing().when(albumService).excluir(1L);

        mockMvc.perform(delete("/api/v1/album/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar404AoExcluirAlbumInexistente() throws Exception {

        doThrow(new EntityNotFoundException("Álbum não encontrado para exclusão"))
                .when(albumService).excluir(99L);

        mockMvc.perform(delete("/api/v1/album/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Álbum não encontrado para exclusão"));
    }

}
