package com.adrianoribeiro.artistas_api.dto;

public class UploadImagemResponseDTO {

    private String url;

    public UploadImagemResponseDTO(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
