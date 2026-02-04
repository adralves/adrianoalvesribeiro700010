package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.dto.AlbumImagemResponseDTO;
import com.adrianoribeiro.artistas_api.service.AlbumImagemService;
import com.adrianoribeiro.artistas_api.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlbumImagemController.class)
@AutoConfigureMockMvc(addFilters = false)
class AlbumImagemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AlbumImagemService albumImagemService;

    @Test
    @DisplayName("Deve fazer upload de imagens com sucesso")
    void deveFazerUploadDeImagensComSucesso() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "foto1.jpg", MediaType.IMAGE_JPEG_VALUE, "bytes1".getBytes()
        );

        List<String> nomesArquivos = List.of("uuid-foto1.jpg");

        when(albumImagemService.adicionarImagens(anyLong(), any(MultipartFile[].class)))
                .thenReturn(nomesArquivos);

        // Rota atualizada para /api/v1/albuns/{id}/imagens
        mockMvc.perform(multipart("/api/v1/albuns/{albumId}/imagens", 1L)
                        .file(file1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urls[0]").value("uuid-foto1.jpg"));
    }

    @Test
    @DisplayName("Deve listar imagens do álbum com seus IDs")
    void deveListarImagensComSucesso() throws Exception {
        List<AlbumImagemResponseDTO> imagens = List.of(
                new AlbumImagemResponseDTO(1L, "http://link-assinado-1.com"),
                new AlbumImagemResponseDTO(2L, "http://link-assinado-2.com")
        );

        when(albumImagemService.listarImagens(1L)).thenReturn(imagens);

        mockMvc.perform(get("/api/v1/albuns/{albumId}/capas", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].url").value("http://link-assinado-1.com"));
    }

    @Test
    @DisplayName("Deve excluir uma imagem com sucesso")
    void deveExcluirImagemComSucesso() throws Exception {
        doNothing().when(albumImagemService).excluirImagem(10L);

        mockMvc.perform(delete("/api/v1/albuns/capas/{imagemId}", 10L))
                .andExpect(status().isNoContent());

        verify(albumImagemService, times(1)).excluirImagem(10L);
    }

    @Test
    @DisplayName("Deve gerar link de download com sucesso")
    void deveGerarLinkDownloadComSucesso() throws Exception {
        String urlFake = "http://localhost/minio/download-link";
        when(albumImagemService.download(10L)).thenReturn(urlFake);

        mockMvc.perform(get("/api/v1/albuns/capas/{imagemId}/download", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadUrl").value(urlFake));
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar upload sem arquivos")
    void deveRetornarBadRequestQuandoNaoEnviarArquivos() throws Exception {
        // MockMvc multipart exige pelo menos um parâmetro ou arquivo para não dar erro de construção
        mockMvc.perform(multipart("/api/v1/albuns/{albumId}/imagens", 1L))
                .andExpect(status().isBadRequest());
    }
}