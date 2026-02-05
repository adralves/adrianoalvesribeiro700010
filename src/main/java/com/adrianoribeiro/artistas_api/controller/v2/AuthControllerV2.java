package com.adrianoribeiro.artistas_api.controller.v2;

import com.adrianoribeiro.artistas_api.dto.LoginResponseDTO;
import com.adrianoribeiro.artistas_api.dto.RefreshTokenRequestDTO;
import com.adrianoribeiro.artistas_api.dto.RefreshTokenResponseDTO;
import com.adrianoribeiro.artistas_api.security.RefreshTokenStore;
import com.adrianoribeiro.artistas_api.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/auth")
@Tag(name = "Autenticação - V2(Basic Auth + JWT)")
public class AuthControllerV2 {

    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;

    @Value("${app.security.username}")
    private String usernameConfig;

    @Value("${app.security.password}")
    private String passwordConfig;

    public AuthControllerV2(JwtService jwtService, RefreshTokenStore refreshTokenStore) {
        this.jwtService = jwtService;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Operation(
            summary = "Login via Basic Auth (v2)",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String decoded = new String(
                Base64.getDecoder().decode(authHeader.substring(6)),
                StandardCharsets.UTF_8
        );

        String[] values = decoded.split(":", 2);
        String username = values[0];
        String password = values[1];

        if (!username.equals(usernameConfig) || !password.equals(passwordConfig)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuário ou senha inválidos");
        }

        String accessToken = jwtService.generateAccessToken(username);
        String refreshToken = jwtService.generateRefreshToken(username);

        refreshTokenStore.salvarToken(refreshToken, username);

        return ResponseEntity.ok(
                new LoginResponseDTO(accessToken, refreshToken)
        );
    }


    @Operation(
            summary = "Refresh token (v2)"
    )
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDTO> refresh(
            @RequestBody RefreshTokenRequestDTO request
    ) {

        String refreshToken = request.refreshToken();

        // valida JWT refresh token
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // valida se ainda está armazenado
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

