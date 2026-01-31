package com.adrianoribeiro.artistas_api.dto;

import java.util.List;

public class UploadAlbumImagensResponseDTO {

    private List<String> urls;

    public UploadAlbumImagensResponseDTO(List<String> urls) {
        this.urls = urls;
    }

    public List<String> getUrls() {
        return urls;
    }
}