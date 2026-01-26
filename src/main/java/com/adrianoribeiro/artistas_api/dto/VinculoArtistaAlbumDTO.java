package com.adrianoribeiro.artistas_api.dto;

import jakarta.validation.constraints.NotNull;

public class VinculoArtistaAlbumDTO {
    @NotNull(message = "O id do artista é obrigatório.")
    private Long artistaId;

    @NotNull(message = "O id do álbum é obrigatório.")
    private Long albumId;

    public VinculoArtistaAlbumDTO() {
    }

    public Long getArtistaId() {
        return artistaId;
    }

    public void setArtistaId(Long artistaId) {
        this.artistaId = artistaId;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }
}
