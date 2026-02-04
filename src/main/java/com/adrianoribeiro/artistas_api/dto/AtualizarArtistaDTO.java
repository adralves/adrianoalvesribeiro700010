package com.adrianoribeiro.artistas_api.dto;

import com.adrianoribeiro.artistas_api.model.enums.TipoArtista;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AtualizarArtistaDTO {
    @Size(max = 255)
    private String nome;

    private TipoArtista tipo;

    public AtualizarArtistaDTO() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoArtista getTipo() {
        return tipo;
    }

    public void setTipo(TipoArtista tipo) {
        this.tipo = tipo;
    }
}
