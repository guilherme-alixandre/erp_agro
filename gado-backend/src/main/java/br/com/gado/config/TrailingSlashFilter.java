package br.com.gado.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * O Spring Framework 6 (usado pelo Spring Boot 4 neste projeto) removeu o
 * casamento automático de barra final ("/api/animais" == "/api/animais/").
 * Este filtro normaliza a URI antes do roteamento, para que ambas as formas
 * continuem funcionando, como acontecia antes dessa mudança de comportamento.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TrailingSlashFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (uri.length() > 1 && uri.endsWith("/")) {
            String semBarra = uri.substring(0, uri.length() - 1);
            filterChain.doFilter(new HttpServletRequestWrapper(request) {
                @Override
                public String getRequestURI() {
                    return semBarra;
                }
            }, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
