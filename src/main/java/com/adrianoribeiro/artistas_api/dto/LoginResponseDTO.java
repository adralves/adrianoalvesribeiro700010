package com.adrianoribeiro.artistas_api.dto;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken
) {}
