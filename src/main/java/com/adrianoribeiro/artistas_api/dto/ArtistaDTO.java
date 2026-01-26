package com.adrianoribeiro.artistas_api.dto;

import com.adrianoribeiro.artistas_api.model.enums.TipoArtista;

public class ArtistaDTO {

    private Long id;
    private String nome;
    private TipoArtista tipo;

    public ArtistaDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
