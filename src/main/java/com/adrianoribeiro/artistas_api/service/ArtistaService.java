package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.repository.AlbumRepository;
import com.adrianoribeiro.artistas_api.repository.ArtistaRepository;
import com.adrianoribeiro.artistas_api.model.Artista;
import com.adrianoribeiro.artistas_api.dto.AtualizarArtistaDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
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
        if (StringUtils.hasText(nome)) {
            return artistaRepository.findByNomeContainingIgnoreCase(nome, pageable);
        }
        return artistaRepository.findAll(pageable);
    }

    public Artista atualizarArtista(Long id, AtualizarArtistaDTO dto) {
        Artista artista = artistaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artista não encontrado"));

        artista.setNome(dto.getNome());
        artista.setTipo(dto.getTipo());

        return artistaRepository.save(artista);

    }


    public void excluir(Long id) {
        Artista artista = artistaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artista não encontrado"));

        if (!artista.getAlbuns().isEmpty()) {
            throw new DataIntegrityViolationException(
                    "Não é possível excluir o artista pois ele está vinculado a " + artista.getAlbuns().size() + " álbum(ns)."
            );
        }

        artistaRepository.delete(artista);
    }
}
