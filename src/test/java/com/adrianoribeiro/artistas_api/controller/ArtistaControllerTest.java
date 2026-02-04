package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.dto.AtualizarArtistaDTO;
import com.adrianoribeiro.artistas_api.model.Artista;
import com.adrianoribeiro.artistas_api.model.enums.TipoArtista;
import com.adrianoribeiro.artistas_api.service.ArtistaService;
import com.adrianoribeiro.artistas_api.service.JwtService;
import jakarta.persistence.EntityNotFoundException;
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

import static org.mockito.ArgumentMatchers.eq;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArtistaController.class)
@Import(ValidationAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class ArtistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private ArtistaService artistaService;

    @Test
    void deveCriarArtistaComSucesso() throws Exception {
        Artista artistaRetorno = new Artista();
        artistaRetorno.setId(1L);
        artistaRetorno.setNome("Mike Shinoda");
        artistaRetorno.setTipo(TipoArtista.CANTOR);

        when(artistaService.criarArtista(any(Artista.class))).thenReturn(artistaRetorno);

        mockMvc.perform(post("/api/v1/artistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nome": "Mike Shinoda",
                              "tipo": "CANTOR"
                            }
                        """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Mike Shinoda"))
                .andExpect(jsonPath("$.tipo").value("CANTOR"));
    }

    @Test
    void deveListarArtistasComSucesso() throws Exception {
        Artista artista = new Artista();
        artista.setId(1L);
        artista.setNome("Mike Shinoda");
        artista.setTipo(TipoArtista.CANTOR);

        Page<Artista> page = new PageImpl<>(List.of(artista));

        when(artistaService.listarArtista(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/artistas")
                        .param("nome", "Mike")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Mike Shinoda"))
                .andExpect(jsonPath("$.content[0].tipo").value("CANTOR"));
    }

    @Test
    void deveAtualizarArtistaComSucesso() throws Exception {
        Artista artistaAtualizado = new Artista();
        artistaAtualizado.setId(1L);
        artistaAtualizado.setNome("Mike Shinoda Atualizado");
        artistaAtualizado.setTipo(TipoArtista.CANTOR);

        when(artistaService.atualizarArtista(eq(1L), any(AtualizarArtistaDTO.class)))
                .thenReturn(artistaAtualizado);

        mockMvc.perform(put("/api/v1/artistas/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nome": "Mike Shinoda Atualizado",
                              "tipo": "CANTOR"
                            }
                        """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Mike Shinoda Atualizado"))
                .andExpect(jsonPath("$.tipo").value("CANTOR"));
    }
    @Test
    void deveExcluirArtistaComSucesso() throws Exception {
        doNothing().when(artistaService).excluir(1L);

        mockMvc.perform(delete("/api/v1/artistas/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(artistaService, times(1)).excluir(1L);
    }

    @Test
    void deveRetornar404AoExcluirArtistaInexistente() throws Exception {
        doThrow(new EntityNotFoundException("Artista não encontrado"))
                .when(artistaService).excluir(99L);

        mockMvc.perform(delete("/api/v1/artistas/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409AoExcluirArtistaComAlbuns() throws Exception {
        // Simulando uma exceção de violação de integridade (comum em Data Integrity)
        doThrow(new IllegalStateException("Conflito: Artista possui álbuns vinculados"))
                .when(artistaService).excluir(1L);

        mockMvc.perform(delete("/api/v1/artistas/{id}", 1L))
                .andExpect(status().isConflict()); // Valida o 409
    }

    @Test
    void deveRetornar400AoCriarArtistaInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/artistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nome": "",
                              "tipo": "INVALIDO"
                            }
                        """))
                .andExpect(status().isBadRequest());
    }
}
