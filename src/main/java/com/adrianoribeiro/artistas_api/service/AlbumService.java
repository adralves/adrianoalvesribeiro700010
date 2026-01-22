package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.model.Artista;
import com.adrianoribeiro.artistas_api.repository.AlbumRepository;
import com.adrianoribeiro.artistas_api.repository.ArtistaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistaRepository artistaRepository;

    public AlbumService(AlbumRepository albumRepository,
                        ArtistaRepository artistaRepository) {
        this.albumRepository = albumRepository;
        this.artistaRepository = artistaRepository;
    }

    public Album criarAlbum(Long artistaId, Album album) {
        Artista artista = artistaRepository.findById(artistaId)
                .orElseThrow(() -> new RuntimeException("Artista não encontrado"));

        album.getArtistas().add(artista);
        artista.getAlbuns().add(album);

        return albumRepository.save(album);
    }

    public List<Album> listarAlbum() {
        return albumRepository.findAll();
    }
}
