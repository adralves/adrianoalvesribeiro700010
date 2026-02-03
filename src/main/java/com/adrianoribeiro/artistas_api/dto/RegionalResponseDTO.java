package com.adrianoribeiro.artistas_api.dto;

import com.adrianoribeiro.artistas_api.model.Regional;

public record RegionalResponseDTO(
        Integer id,
        String nome
) {
    public static RegionalResponseDTO fromEntity(Regional regional) {
        return new RegionalResponseDTO(
                regional.getRegionalId(),
                regional.getNome()
        );
    }
}
