package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.dto.AlbumImagemResponseDTO;
import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.model.AlbumImagem;
import com.adrianoribeiro.artistas_api.repository.AlbumImagemRepository;
import com.adrianoribeiro.artistas_api.repository.AlbumRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlbumImagemService {

    private final AlbumRepository albumRepository;
    private final AlbumImagemRepository albumImagemRepository;
    private final MinioService minioService;

    public AlbumImagemService(
            AlbumRepository albumRepository,
            AlbumImagemRepository albumImagemRepository,
            MinioService minioService
    ) {
        this.albumRepository = albumRepository;
        this.albumImagemRepository = albumImagemRepository;
        this.minioService = minioService;
    }

    @Transactional
    public List<String> adicionarImagens(Long albumId, MultipartFile[] files) {

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Álbum não encontrado"
                ));

        List<String> urls = new ArrayList<>();

        for (MultipartFile file : files) {
            String url = minioService.uploadImagem(file);

            AlbumImagem imagem = new AlbumImagem(url, album);
            albumImagemRepository.save(imagem);

            urls.add(url);
        }

        return urls;
    }

    @Transactional(readOnly = true)
    public List<AlbumImagemResponseDTO> listarImagens(Long albumId) {
        if (!albumRepository.existsById(albumId)) {
            throw new EntityNotFoundException("Álbum não encontrado");
        }

        return albumImagemRepository.findByAlbumId(albumId).stream()
                .map(img -> new AlbumImagemResponseDTO(
                        img.id(),
                        minioService.gerarUrl(img.url()) // Gera a URL assinada
                ))
                .toList();
    }

    @Transactional
    public void excluirImagem(Long imagemId) {
        AlbumImagem imagem = albumImagemRepository.findById(imagemId)
                .orElseThrow(() -> new EntityNotFoundException("Imagem não encontrada"));

        // Agora 'imagem.getUrl()' na verdade contém o nome do arquivo: "527502d5...png"
        minioService.removerArquivo(imagem.getUrl());

        albumImagemRepository.delete(imagem);
    }

    @Transactional(readOnly = true)
    public String download(Long imagemId) {
        AlbumImagem imagem = albumImagemRepository.findById(imagemId)
                .orElseThrow(() -> new EntityNotFoundException("Imagem não encontrada"));

        // Retorna a URL assinada que força o download
        return minioService.download(imagem.getUrl());
    }
}