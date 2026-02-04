package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.dto.AtualizarArtistaDTO;
import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.model.Artista;
import com.adrianoribeiro.artistas_api.model.enums.TipoArtista;
import com.adrianoribeiro.artistas_api.repository.AlbumRepository;
import com.adrianoribeiro.artistas_api.repository.ArtistaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistaServiceTest {

    @Mock
    private ArtistaRepository artistaRepository;

    @Mock
    private AlbumRepository albumRepository;

    @InjectMocks
    private ArtistaService artistaService;

    @Test
    void deveCriarArtistaComSucesso() {
        Artista artista = new Artista();
        artista.setNome("Mike Shinoda");
        artista.setTipo(TipoArtista.CANTOR);

        Artista artistaSalvo = new Artista();
        artistaSalvo.setId(1L);
        artistaSalvo.setNome("Mike Shinoda");
        artistaSalvo.setTipo(TipoArtista.CANTOR);

        when(artistaRepository.save(any(Artista.class))).thenReturn(artistaSalvo);

        Artista resultado = artistaService.criarArtista(artista);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Mike Shinoda", resultado.getNome());
        assertEquals(TipoArtista.CANTOR, resultado.getTipo());

        verify(artistaRepository, times(1)).save(artista);
    }

    @Test
    void deveListarArtistasSemFiltro() {
        Artista artista = new Artista();
        artista.setNome("Mike Shinoda");

        Page<Artista> page = new PageImpl<>(List.of(artista));
        when(artistaRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Artista> resultado = artistaService.listarArtista(null, PageRequest.of(0, 10));

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Mike Shinoda", resultado.getContent().get(0).getNome());
    }

    @Test
    void deveListarArtistasComFiltroPorNome() {
        Artista artista = new Artista();
        artista.setNome("Mike Shinoda");

        Page<Artista> page = new PageImpl<>(List.of(artista));
        when(artistaRepository.findByNomeContainingIgnoreCase(eq("Mike"), any(Pageable.class)))
                .thenReturn(page);

        Page<Artista> resultado = artistaService.listarArtista("Mike", PageRequest.of(0, 10));

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Mike Shinoda", resultado.getContent().get(0).getNome());
    }

    @Test
    void deveAtualizarArtistaComSucesso() {
        Artista artistaExistente = new Artista();
        artistaExistente.setId(1L);
        artistaExistente.setNome("Mike Shinoda");
        artistaExistente.setTipo(TipoArtista.CANTOR);

        AtualizarArtistaDTO dto = new AtualizarArtistaDTO();
        dto.setNome("Mike Atualizado");
        dto.setTipo(TipoArtista.CANTOR);

        when(artistaRepository.findById(1L)).thenReturn(Optional.of(artistaExistente));
        when(artistaRepository.save(any(Artista.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Artista resultado = artistaService.atualizarArtista(1L, dto);

        assertEquals("Mike Atualizado", resultado.getNome());
        verify(artistaRepository, times(1)).save(artistaExistente);
    }

    @Test
    void deveLancarExcecaoAoAtualizarArtistaInexistente() {
        when(artistaRepository.findById(1L)).thenReturn(Optional.empty());

        AtualizarArtistaDTO dto = new AtualizarArtistaDTO();
        dto.setNome("Mike Atualizado");
        dto.setTipo(TipoArtista.CANTOR);

        assertThrows(RuntimeException.class, () -> artistaService.atualizarArtista(1L, dto));
    }

}