package com.adrianoribeiro.artistas_api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        String path = request.getRequestURI();

        // Se o acesso for à página de monitoramento
        if (path.endsWith("/monitor.html")) {
            // 1. Define o cabeçalho de DESAFIO (isso faz o pop-up abrir)
            response.setHeader("WWW-Authenticate", "Basic realm=\"Monitoramento\"");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/html;charset=UTF-8");

            // 2. Texto simples para o navegador
            response.getWriter().write("<html><body><h1>401 - Autenticação Necessária</h1>" +
                    "<p>Por favor, recarregue a página e insira as credenciais.</p></body></html>");
            return;
        }

        // --- COMPORTAMENTO PADRÃO PARA API (JSON) ---
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(String.format("""
            {
              "error": "Unauthorized",
              "message": "%s",
              "path": "%s"
            }
            """, "Você precisa estar autenticado para acessar este recurso", path));
    }
}