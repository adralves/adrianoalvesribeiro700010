package com.adrianoribeiro.artistas_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AtualizarAlbumDTO {
    @NotBlank(message = "O nome do álbum é obrigatório.")
    @Size(max = 255)
    private String nome;

    public AtualizarAlbumDTO() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
