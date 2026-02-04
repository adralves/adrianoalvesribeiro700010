package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.model.enums.TipoArtista;
import com.adrianoribeiro.artistas_api.repository.AlbumRepository;
import com.adrianoribeiro.artistas_api.repository.ArtistaRepository;
import com.adrianoribeiro.artistas_api.dto.AtualizarAlbumDTO;
import com.adrianoribeiro.artistas_api.websocket.AlbumNotificationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistaRepository artistaRepository;
    private final AlbumNotificationService notificationService;

    public AlbumService(AlbumRepository albumRepository,
                        ArtistaRepository artistaRepository, AlbumNotificationService notificationService) {
        this.albumRepository = albumRepository;
        this.artistaRepository = artistaRepository;
        this.notificationService = notificationService;
    }

    public Album criarAlbum(Album album) {
        Album salvo = albumRepository.save(album);

        // Notifica o front em tempo real quando criar um novo album
        notificationService.notificarNovoAlbum(salvo);
        return salvo;
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

    public Page<Album> listarAlbunsPorTipoArtista(TipoArtista tipo, Pageable pageable) {
        return albumRepository.findByTipoArtista(tipo, pageable);
    }

    public Page<Album> listaAlbunsPorArtista(String artista, Pageable pageable) {
        return albumRepository.listaAlbunsPorArtista(artista, pageable);
    }

    public Album buscarPorId(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Álbum não encontrado para o ID informado"
                ));
    }

    public void excluir(Long id) {

        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Álbum não encontrado para exclusão"
                ));

        albumRepository.delete(album);
    }

    public List<Album> listarAlbunsDoArtista(Long artistaId) {
        if (!artistaRepository.existsById(artistaId)) {
            throw new EntityNotFoundException("Artista não encontrado");
        }

        return albumRepository.findAlbunsByArtistaId(artistaId);
    }

}
