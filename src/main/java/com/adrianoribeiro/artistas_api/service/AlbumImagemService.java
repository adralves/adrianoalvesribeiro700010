package com.adrianoribeiro.artistas_api.service;

import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.model.AlbumImagem;
import com.adrianoribeiro.artistas_api.repository.AlbumImagemRepository;
import com.adrianoribeiro.artistas_api.repository.AlbumRepository;
import jakarta.transaction.Transactional;
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
    public List<String> uploadCapas(Long albumId, MultipartFile[] files) {

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
}