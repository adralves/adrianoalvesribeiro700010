package com.adrianoribeiro.artistas_api.dto;

public class AlbumNotificacaoDTO {

    private Long id;
    private String nome;
    private String mensagem;

    public AlbumNotificacaoDTO(Long id, String nome, String mensagem) {
        this.id = id;
        this.nome = nome;
        this.mensagem = mensagem;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getMensagem() {
        return mensagem;
    }
}
