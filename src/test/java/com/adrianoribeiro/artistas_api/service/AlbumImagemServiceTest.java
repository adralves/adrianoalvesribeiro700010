package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.model.AlbumImagem;
import com.adrianoribeiro.artistas_api.repository.AlbumImagemRepository;
import com.adrianoribeiro.artistas_api.repository.AlbumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlbumImagemServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private AlbumImagemRepository albumImagemRepository;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private AlbumImagemService albumImagemService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveFazerUploadDeCapasComSucesso() {
        Album album = new Album();
        album.setId(1L);
        album.setNome("Rock Hits");

        MockMultipartFile file1 = new MockMultipartFile(
                "file", "capa1.jpg", "image/jpeg", "conteudo1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file", "capa2.jpg", "image/jpeg", "conteudo2".getBytes()
        );

        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(minioService.uploadImagem(file1)).thenReturn("http://s3/capa1.jpg");
        when(minioService.uploadImagem(file2)).thenReturn("http://s3/capa2.jpg");

        List<String> urls = albumImagemService.uploadCapas(1L, new MockMultipartFile[]{file1, file2});

        assertEquals(2, urls.size());
        assertEquals("http://s3/capa1.jpg", urls.get(0));
        assertEquals("http://s3/capa2.jpg", urls.get(1));

        verify(albumImagemRepository, times(2)).save(any(AlbumImagem.class));
    }

    @Test
    void deveLancarExcecaoQuandoAlbumNaoExiste() {
        when(albumRepository.findById(1L)).thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile(
                "file", "capa.jpg", "image/jpeg", "conteudo".getBytes()
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> albumImagemService.uploadCapas(1L, new MockMultipartFile[]{file})
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Álbum não encontrado", exception.getReason());

        verify(albumImagemRepository, never()).save(any());
        verify(minioService, never()).uploadImagem(any());
    }
}
