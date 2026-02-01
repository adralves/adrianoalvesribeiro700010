package com.adrianoribeiro.artistas_api.controller;

import com.adrianoribeiro.artistas_api.controller.v2.AuthControllerV2;
import com.adrianoribeiro.artistas_api.security.RefreshTokenStore;
import com.adrianoribeiro.artistas_api.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthControllerV2.class)
@Import({})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerV2Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RefreshTokenStore refreshTokenStore;

    // valores de configuração simulados
    @Value("${app.security.username}")
    private String usernameConfig = "admin";

    @Value("${app.security.password}")
    private String passwordConfig = "1234";

    private String basicAuthHeader(String user, String pass) {
        String credentials = user + ":" + pass;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void deveLogarComSucesso() throws Exception {
        String username = "seletivo";
        String password = "admin";
        String accessToken = "tokenAcesso";
        String refreshToken = "tokenRefresh";

        when(jwtService.generateAccessToken(username)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(username)).thenReturn(refreshToken);

        mockMvc.perform(post("/api/v2/auth/login")
                        .header("Authorization", basicAuthHeader(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andExpect(jsonPath("$.refreshToken").value(refreshToken));

        verify(jwtService, times(1)).generateAccessToken(username);
        verify(jwtService, times(1)).generateRefreshToken(username);
        verify(refreshTokenStore, times(1)).salvarToken(refreshToken, username);
    }

    @Test
    void deveFalharLoginSeCredenciaisInvalidas() throws Exception {
        mockMvc.perform(post("/api/v2/auth/login")
                        .header("Authorization", basicAuthHeader("userErrado", "senhaErrada")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Usuário ou senha inválidos"));
    }

    @Test
    void deveFalharLoginSeHeaderAusente() throws Exception {
        mockMvc.perform(post("/api/v2/auth/login"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveGerarNovoAccessTokenComRefreshTokenValido() throws Exception {
        String refreshToken = "tokenRefresh";
        String username = "admin";
        String newAccessToken = "novoTokenAcesso";

        when(jwtService.isRefreshTokenValid(refreshToken)).thenReturn(true);
        when(refreshTokenStore.buscarUsuario(refreshToken)).thenReturn(username);
        when(jwtService.generateAccessToken(username)).thenReturn(newAccessToken);

        mockMvc.perform(post("/api/v2/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken));
    }

    @Test
    void deveFalharRefreshSeTokenInvalido() throws Exception {
        String refreshToken = "tokenInvalido";
        when(jwtService.isRefreshTokenValid(refreshToken)).thenReturn(false);

        mockMvc.perform(post("/api/v2/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveFalharRefreshSeTokenNaoArmazenado() throws Exception {
        String refreshToken = "tokenNaoArmazenado";
        when(jwtService.isRefreshTokenValid(refreshToken)).thenReturn(true);
        when(refreshTokenStore.buscarUsuario(refreshToken)).thenReturn(null);

        mockMvc.perform(post("/api/v2/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}