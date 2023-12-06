package com.api.gerenciadorprojetos.Infra.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

/**
 * Filtro de segurança para autenticação com token JWT.
 *
 * @author victor.marcelo
 */

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret; // Chave secreta para assinar e verificar o token JWT

    /**
     * Método principal para processar a requisição e realizar a autenticação com token JWT.
     *
     * @param request     O objeto HttpServletRequest
     * @param response    O objeto HttpServletResponse
     * @param filterChain O objeto FilterChain para encadear a execução dos filtros
     * @throws ServletException Em caso de erro no servlet
     * @throws IOException      Em caso de erro de I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwtToken = extractJwtToken(request);

            if (jwtToken != null && Jwts.parser().setSigningKey(jwtSecret).isSigned(jwtToken)) {
                Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwtToken).getBody();

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, null);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token expirado");
            return;
        }

        filterChain.doFilter(request, response);
    }



    /**
     * Extrai o token JWT do cabeçalho "Authorization" da requisição.
     *
     * @param request O objeto HttpServletRequest
     * @return O token JWT ou null se não estiver presente no cabeçalho ou se estiver em formato inválido
     */
    private String extractJwtToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

}
