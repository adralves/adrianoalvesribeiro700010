package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.model.enums.TipoArtista;
import com.adrianoribeiro.artistas_api.repository.AlbumRepository;
import com.adrianoribeiro.artistas_api.repository.ArtistaRepository;
import com.adrianoribeiro.artistas_api.dto.AtualizarAlbumDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistaRepository artistaRepository;

    public AlbumService(AlbumRepository albumRepository,
                        ArtistaRepository artistaRepository) {
        this.albumRepository = albumRepository;
        this.artistaRepository = artistaRepository;
    }

    public Album criarAlbum(Album album) {
        return albumRepository.save(album);
    }

    public Page<Album> listarAlbuns(String nome, Pageable pageable) {
        if (nome != null && !nome.isBlank()) {
            return albumRepository.findByNomeContainingIgnoreCase(nome, pageable);
        }
        return albumRepository.findAll(pageable);    }

    public Album atualizarAlbum(Long id, AtualizarAlbumDTO dto) {

        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado"));

        album.setNome(dto.getNome());

        return albumRepository.save(album);
    }

    public Page<Album> listarPorTipoArtista(TipoArtista tipo, Pageable pageable) {
        return albumRepository.findByTipoArtista(tipo, pageable);
    }

    public Page<Album> listaAlbunsPorArtista(String artista, Pageable pageable) {
        return albumRepository.listaAlbunsPorArtista(artista, pageable);
    }

}
