package com.adrianoribeiro.artistas_api.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setup() throws Exception {
        jwtService = new JwtService();

        // Setando valores privados @Value via reflection
        Field secretKeyField = JwtService.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtService, "minhachavesecreta12345678901234567890"); // chave > 32 chars

        Field accessField = JwtService.class.getDeclaredField("accessExpiration");
        accessField.setAccessible(true);
        accessField.set(jwtService, 1000L * 60 * 5); // 5 minutos

        Field refreshField = JwtService.class.getDeclaredField("refreshExpiration");
        refreshField.setAccessible(true);
        refreshField.set(jwtService, 1000L * 60 * 60); // 1 hora
    }

    @Test
    void deveGerarETestarAccessToken() {
        String username = "user123";
        String token = jwtService.generateAccessToken(username);

        assertTrue(jwtService.isAccessTokenValid(token));
        assertEquals(username, jwtService.extractUsername(token));
        assertFalse(jwtService.isRefreshTokenValid(token)); // não é refresh
    }

    @Test
    void deveGerarETestarRefreshToken() {
        String username = "user123";
        String token = jwtService.generateRefreshToken(username);

        assertTrue(jwtService.isRefreshTokenValid(token));
        assertEquals(username, jwtService.extractUsername(token));
        assertFalse(jwtService.isAccessTokenValid(token)); // não é access
    }

    @Test
    void deveInvalidarTokenExpirado() throws Exception {
        // Gerando token com 1ms de expiração para forçar expiração
        Field accessField = JwtService.class.getDeclaredField("accessExpiration");
        accessField.setAccessible(true);
        accessField.set(jwtService, 1L);

        String token = jwtService.generateAccessToken("user123");

        // Dormir 10ms para garantir que o token expirou
        Thread.sleep(10);

        assertFalse(jwtService.isAccessTokenValid(token));
    }

    @Test
    void deveInvalidarTokenCorrompido() {
        String token = "tokeninvalido";
        assertFalse(jwtService.isAccessTokenValid(token));
        assertFalse(jwtService.isRefreshTokenValid(token));
    }
}
