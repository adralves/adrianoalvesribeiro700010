package com.adrianoribeiro.artistas_api.dto;

import com.adrianoribeiro.artistas_api.model.Regional;

public record RegionalResponse(
        Integer id,
        String nome
) {
    public static RegionalResponse fromEntity(Regional regional) {
        return new RegionalResponse(
                regional.getRegionalId(),
                regional.getNome()
        );
    }
}
