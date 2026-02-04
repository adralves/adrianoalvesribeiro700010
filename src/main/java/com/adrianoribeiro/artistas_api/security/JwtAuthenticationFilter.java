package com.adrianoribeiro.artistas_api.security;

import com.adrianoribeiro.artistas_api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Value("${app.security.username}")
    private String usernameConfig;

    @Value("${app.security.password}")
    private String passwordConfig;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Rotas que NÃO devem passar pelo JWT
     * WebSocket, Swagger, Actuator etc.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();

        return path.startsWith("/ws")
                || path.startsWith("/topic")
                || path.startsWith("/app")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")
                || path.startsWith("/api/v2/auth")
                || path.startsWith("/api/v2/auth/refresh")
                || path.startsWith("/api/v1/auth/refresh")
                || path.startsWith("/api/v1/auth/login");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // OPTIONS passa direto (CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // ==========================
        // BEARER JWT
        // ==========================
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);
            try {
                if (jwtService.isAccessTokenValid(token)) {

                    String username = jwtService.extractUsername(token);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                } else {
                    SecurityContextHolder.clearContext();
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
                    return;
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
                return;
            }

        }

        // ==========================
        // BASIC AUTH
        // ==========================
        else if (authHeader != null && authHeader.startsWith("Basic ")) {

            try {
                String base64 = authHeader.substring(6);
                String decoded = new String(
                        Base64.getDecoder().decode(base64),
                        StandardCharsets.UTF_8
                );

                String[] values = decoded.split(":", 2);
                String username = values[0];
                String password = values[1];

                if (username.equals(usernameConfig)
                        && password.equals(passwordConfig)) {

                    SecurityContextHolder.getContext()
                            .setAuthentication(
                                    new UsernamePasswordAuthenticationToken(
                                            username,
                                            null,
                                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                    )
                            );
                }
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
//        if (SecurityContextHolder.getContext().getAuthentication() == null) {
//            response.sendError(
//                    HttpServletResponse.SC_UNAUTHORIZED,
//                    "Você precisa estar autenticado para acessar este recurso"
//            );
//            return;
//        }

        filterChain.doFilter(request, response);
    }
}
