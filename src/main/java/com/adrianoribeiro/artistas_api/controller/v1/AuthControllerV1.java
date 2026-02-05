package com.adrianoribeiro.artistas_api.controller.v1;

import com.adrianoribeiro.artistas_api.dto.LoginRequestDTO;
import com.adrianoribeiro.artistas_api.dto.LoginResponseDTO;
import com.adrianoribeiro.artistas_api.dto.RefreshTokenRequestDTO;
import com.adrianoribeiro.artistas_api.dto.RefreshTokenResponseDTO;
import com.adrianoribeiro.artistas_api.security.RefreshTokenStore;
import com.adrianoribeiro.artistas_api.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação - V1(login JSON + JWT)")
public class AuthControllerV1 {

    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;

    @Value("${app.security.username}")
    private String usernameConfig;

    @Value("${app.security.password}")
    private String passwordConfig;

    public AuthControllerV1(JwtService jwtService, RefreshTokenStore refreshTokenStore) {
        this.jwtService = jwtService;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Operation(
            summary = "Login JSON (v1)"
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDTO request
    ) {

        if (!request.username().equals(usernameConfig)
                || !request.password().equals(passwordConfig)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuário ou senha inválidos");
        }

        String accessToken = jwtService.generateAccessToken(request.username());
        String refreshToken = jwtService.generateRefreshToken(request.username());

        refreshTokenStore.salvarToken(refreshToken, request.username());

        return ResponseEntity.ok(
                new LoginResponseDTO(accessToken, refreshToken)
        );
    }

    @Operation(
            summary = "Refresh token (v1)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDTO> refreshToken(
            @RequestBody RefreshTokenRequestDTO request
    ) {

        String refreshToken = request.refreshToken();

        System.out.println("JWT valido: " + jwtService.isRefreshTokenValid(refreshToken));
        System.out.println("No store: " + refreshTokenStore.buscarUsuario(refreshToken));

        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = refreshTokenStore.buscarUsuario(refreshToken);

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String newAccessToken = jwtService.generateAccessToken(username);

        return ResponseEntity.ok(
                new RefreshTokenResponseDTO(newAccessToken)
        );
    }
}
