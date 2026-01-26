package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.model.Artista;
import com.adrianoribeiro.artistas_api.repository.AlbumRepository;
import com.adrianoribeiro.artistas_api.repository.ArtistaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ArtistaAlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistaRepository artistaRepository;

    public ArtistaAlbumService(AlbumRepository albumRepository,
                               ArtistaRepository artistaRepository) {
        this.albumRepository = albumRepository;
        this.artistaRepository = artistaRepository;
    }

    @Transactional
    public Album vincularArtistaAlbum(Long artistaId, Long albumId) {

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Álbum não encontrado"));

        Artista artista = artistaRepository.findById(artistaId)
                .orElseThrow(() -> new EntityNotFoundException("Artista não encontrado"));

        album.getArtistas().add(artista);

        return albumRepository.save(album);
    }
}

