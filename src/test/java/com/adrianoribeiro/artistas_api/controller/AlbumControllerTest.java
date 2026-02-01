package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.model.enums.TipoArtista;
import com.adrianoribeiro.artistas_api.service.AlbumService;
import com.adrianoribeiro.artistas_api.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
}
