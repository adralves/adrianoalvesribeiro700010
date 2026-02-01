package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.service.AlbumImagemService;
import com.adrianoribeiro.artistas_api.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
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
    void deveFazerUploadDeCapasComSucesso() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "capa1.jpg", MediaType.IMAGE_JPEG_VALUE, "conteudo1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "files", "capa2.jpg", MediaType.IMAGE_JPEG_VALUE, "conteudo2".getBytes()
        );

        List<String> urls = List.of("http://s3/capa1.jpg", "http://s3/capa2.jpg");

        when(albumImagemService.uploadCapas(anyLong(), any(MultipartFile[].class)))
                .thenReturn(urls);

        mockMvc.perform(multipart("/api/v1/albuns/{albumId}/capas", 1L)
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urls[0]").value("http://s3/capa1.jpg"))
                .andExpect(jsonPath("$.urls[1]").value("http://s3/capa2.jpg"));
    }

    @Test
    void deveRetornarBadRequestQuandoNaoEnviarArquivos() throws Exception {
        mockMvc.perform(multipart("/api/v1/albuns/{albumId}/capas", 1L))
                .andExpect(status().isBadRequest());
    }
}
