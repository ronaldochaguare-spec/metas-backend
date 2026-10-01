package com.metas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * Exige "Authorization: Bearer <token>" en /api/ganancias/**.
 * /api/login queda abierto porque es donde se consigue el token.
 */
@Component
public class JwtFilter implements jakarta.servlet.Filter {

    private final JwtService jwt;

    public JwtFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) resp;

        String path = r.getRequestURI();
        boolean protegido = path.startsWith("/api/ganancias");
        boolean esPreflight = "OPTIONS".equalsIgnoreCase(r.getMethod());

        if (!protegido || esPreflight) {
            chain.doFilter(req, resp);
            return;
        }

        String header = r.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;

        if (token == null || !jwt.esValido(token)) {
            w.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            w.setContentType("application/json");
            w.getWriter().write("{\"error\":\"No autorizado\"}");
            return;
        }
        chain.doFilter(req, resp);
    }
}
