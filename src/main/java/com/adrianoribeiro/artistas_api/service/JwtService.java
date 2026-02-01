package com.adrianoribeiro.artistas_api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-expiration}")
    private long accessExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    public String generateAccessToken(String username) {
        return buildToken(username, "ACCESS", accessExpiration);
    }

    public String generateRefreshToken(String username) {
        return buildToken(username, "REFRESH", refreshExpiration);
    }

    private String buildToken(String username, String type, long expiration) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", type)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isAccessTokenValid(String token) {
        return isTokenValid(token, "ACCESS");
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token, "REFRESH");
    }
    private boolean isTokenValid(String token, String expectedType) {
        try {
            Claims claims = getAllClaims(token);

            boolean notExpired = claims.getExpiration().after(new Date());
            boolean correctType = expectedType.equals(claims.get("type"));

            return notExpired && correctType;

        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }

}