package com.adrianoribeiro.artistas_api.resource;

import com.adrianoribeiro.artistas_api.model.Album;
import com.adrianoribeiro.artistas_api.service.AlbumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/albuns")
public class AlbumResource {

    private final AlbumService albumService;

    public AlbumResource(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PostMapping("/artista/{artistaId}")
    public ResponseEntity<Album> criarAlbum(
            @PathVariable Long artistaId,
            @RequestBody Album album) {

        return ResponseEntity.ok(albumService.criarAlbum(artistaId, album));
    }

    @GetMapping
    private List<Album> listarAlbum(){
        return albumService.listarAlbum();
    }
}