package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.repository.AlbumRepository;
import com.adrianoribeiro.artistas_api.repository.ArtistaRepository;
import com.adrianoribeiro.artistas_api.model.Artista;
import com.adrianoribeiro.artistas_api.dto.AtualizarArtistaDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArtistaService {

    private final ArtistaRepository artistaRepository;
    private final AlbumRepository albumRepository;

    public ArtistaService(ArtistaRepository artistaRepository, AlbumRepository albumRepository) {
        this.artistaRepository = artistaRepository;
        this.albumRepository = albumRepository;
    }

    public Artista criarArtista(Artista artista){
        return artistaRepository.save(artista);
    }

    public Page<Artista> listarArtista(String nome, Pageable pageable) {

        Pageable pageableFinal = pageable;

        if (pageable.getSort().isUnsorted()) {
            pageableFinal = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("nome").ascending()
            );
        }

        if (nome != null && !nome.isBlank()) {
            return artistaRepository.findByNomeContainingIgnoreCase(nome, pageableFinal);
        }

        return artistaRepository.findAll(pageableFinal);
    }


    public Artista atualizarArtista(Long id, AtualizarArtistaDTO dto) {
        Artista artista = artistaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artista não encontrado"));

        artista.setNome(dto.getNome());
        artista.setTipo(dto.getTipo());

        return artistaRepository.save(artista);

    }

    public List<Album> listarAlbunsArtista(Long artistaId) {
        if (!artistaRepository.existsById(artistaId)) {
            throw new EntityNotFoundException("Artista não encontrado");
        }

        return albumRepository.findAlbunsByArtistaId(artistaId);
    }

}
