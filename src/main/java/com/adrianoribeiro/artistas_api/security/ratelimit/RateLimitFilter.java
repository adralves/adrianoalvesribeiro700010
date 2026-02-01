package com.adrianoribeiro.artistas_api.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Ignorar preflight CORS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        //  Ignorar endpoints técnicos
        String path = request.getRequestURI();
        if (path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")
                || path.startsWith("/api/v1/auth")) {

            filterChain.doFilter(request, response);
            return;
        }

        // Resolver usuário de forma estável
        String userKey = resolveUserKey(request);

        Bucket bucket = buckets.computeIfAbsent(userKey, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            response.getWriter().write("""
                {
                  "error": "RATE_LIMIT_EXCEEDED",
                  "message": "Máximo de 10 requisições por minuto"
                }
            """);
        }
    }

    private Bucket createNewBucket(String key) {
        Bandwidth limit = Bandwidth.classic(
                10,
                Refill.intervally(10, Duration.ofMinutes(1))
        );

        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Define quem é o "usuário"
     * Prioridade:
     * username do JWT
     * IP como fallback
     */
    private String resolveUserKey(HttpServletRequest request) {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }

        return request.getRemoteAddr();
    }
}
