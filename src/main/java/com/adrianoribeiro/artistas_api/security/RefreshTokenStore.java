package com.adrianoribeiro.artistas_api.security;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RefreshTokenStore {

    private final Map<String, String> tokens = new HashMap<>();

    public void salvarToken(String refreshToken, String username) {
        tokens.put(refreshToken, username);
    }

    public String buscarUsuario(String refreshToken) {
        return tokens.get(refreshToken);
    }

    public void apagarToken(String refreshToken) {
        tokens.remove(refreshToken);
    }
}
