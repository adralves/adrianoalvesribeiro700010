package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.dto.AtualizarAlbumDTO;
import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.model.enums.TipoArtista;
import com.adrianoribeiro.artistas_api.repository.AlbumRepository;
import com.adrianoribeiro.artistas_api.repository.ArtistaRepository;
import com.adrianoribeiro.artistas_api.websocket.AlbumNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistaRepository artistaRepository;

    @Mock
    private AlbumNotificationService notificationService;

    @InjectMocks
    private AlbumService albumService;

    @Test
    void deveCriarAlbumEEnviarNotificacao() {

        // Arrange (preparação)
        Album album = new Album();
        album.setNome("Rock Beats 2026");

        Album albumSalvo = new Album();
        albumSalvo.setId(1L);
        albumSalvo.setNome("Rock Beats 2026");

        when(albumRepository.save(any(Album.class)))
                .thenReturn(albumSalvo);

        // Act (ação)
        Album resultado = albumService.criarAlbum(album);

        // Assert (verificações)
        assertNotNull(resultado);
        assertEquals("Rock Beats 2026", resultado.getNome());

        verify(albumRepository, times(1)).save(album);
        verify(notificationService, times(1))
                .notificarNovoAlbum(albumSalvo);
    }

    @Test
    void deveListarAlbunsSemFiltro() {
        Page<Album> page = new PageImpl<>(List.of(new Album()));

        when(albumRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<Album> resultado = albumService.listarAlbuns(null, PageRequest.of(0, 10));

        assertEquals(1, resultado.getTotalElements());
    }


    @Test
    void deveListarAlbunsComFiltroPorNome() {
        Page<Album> page = new PageImpl<>(List.of(new Album()));

        when(albumRepository.findByNomeContainingIgnoreCase(
                eq("rock"), any(Pageable.class)))
                .thenReturn(page);

        Page<Album> resultado = albumService.listarAlbuns("rock", PageRequest.of(0, 10));

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void deveLancarExcecaoAoAtualizarAlbumInexistente() {

        when(albumRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                albumService.atualizarAlbum(1L, new AtualizarAlbumDTO())
        );
    }

    @Test
    void deveAtualizarAlbumComSucesso() {
        Album albumExistente = new Album();
        albumExistente.setId(1L);
        albumExistente.setNome("Rock Antigo");

        AtualizarAlbumDTO dto = new AtualizarAlbumDTO();
        dto.setNome("Rock Atualizado");

        when(albumRepository.findById(1L)).thenReturn(Optional.of(albumExistente));
        when(albumRepository.save(any(Album.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Album resultado = albumService.atualizarAlbum(1L, dto);

        assertEquals("Rock Atualizado", resultado.getNome());
        verify(albumRepository, times(1)).save(albumExistente);
    }

    @Test
    void deveListarPorTipoArtista() {
        Page<Album> page = new PageImpl<>(List.of(new Album()));
        when(albumRepository.findByTipoArtista(eq(TipoArtista.CANTOR), any(Pageable.class)))
                .thenReturn(page);

        Page<Album> resultado = albumService.listarPorTipoArtista(TipoArtista.CANTOR, PageRequest.of(0, 10));

        assertNotNull(resultado);
        verify(albumRepository).findByTipoArtista(eq(TipoArtista.CANTOR), any());
    }

    @Test
    void deveListarAlbunsPorNomeArtistaNoService() {
        // Arrange
        String nomeBusca = "Linkin Park";
        Pageable pageable = PageRequest.of(0, 10);

        Album album = new Album();
        album.setNome("Hybrid Theory");
        Page<Album> pageMock = new PageImpl<>(List.of(album));

        when(albumRepository.listaAlbunsPorArtista(eq(nomeBusca), any(Pageable.class)))
                .thenReturn(pageMock);

        // Act
        Page<Album> resultado = albumService.listaAlbunsPorArtista(nomeBusca, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Hybrid Theory", resultado.getContent().get(0).getNome());

        verify(albumRepository, times(1)).listaAlbunsPorArtista(nomeBusca, pageable);
    }
}
