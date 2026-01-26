package com.adrianoribeiro.artistas_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CriarAlbumDTO {

    @NotBlank(message = "O nome do álbum é obrigatório.")
    @Size(max = 255, message = "O nome do álbum deve ter no máximo 255 caracteres.")
    private String nome;

    public CriarAlbumDTO() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
