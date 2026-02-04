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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum não encontrado"));

        List<String> urls = new ArrayList<>();

        for (MultipartFile file : files) {
            // Validação de tipo de arquivo
            validarSeArquivoEImagem(file);

            String url = minioService.uploadImagem(file);
            AlbumImagem imagem = new AlbumImagem(url, album);
            albumImagemRepository.save(imagem);
            urls.add(url);
        }
        return urls;
    }

    @Transactional
    public String atualizarImagem(Long imagemId, MultipartFile file) {
        validarSeArquivoEImagem(file);

        AlbumImagem imagemExistente = albumImagemRepository.findById(imagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada"));

        // 1. Remove a imagem antiga do MinIO para não deixar lixo
        minioService.removerArquivo(imagemExistente.getUrl());

        // 2. Faz o upload da nova imagem
        String novaUrl = minioService.uploadImagem(file);

        // 3. Atualiza o registro no banco
        imagemExistente.setUrl(novaUrl);
        albumImagemRepository.save(imagemExistente);

        return novaUrl;
    }

    private void validarSeArquivoEImagem(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O arquivo " + file.getOriginalFilename() + " não é uma imagem válida.");
        }
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