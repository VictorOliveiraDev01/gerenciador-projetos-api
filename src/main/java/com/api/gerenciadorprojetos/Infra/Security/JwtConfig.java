package com.api.gerenciadorprojetos.Infra.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

/**
 * Config para o token JWT.
 *
 * @author victor.marcelo
 */
@Configuration
@EnableWebSecurity
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Método que cria um objeto SecretKey a partir da chave secreta configurada.
     *
     * @return A SecretKey gerada a partir da chave secreta.
     */
    @Bean
    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Método que cria um objeto JwtTokenProvider configurado com oque é necessário.
     *
     * @param userDetailsService Para carregar as informações do usuário.
     * @param secretKey A chave secreta usada para assinar os tokens.
     * @return Um JwtTokenProvider configurado.
     */
    @Bean
    public JwtTokenProvider jwtTokenProvider(UserDetailsService userDetailsService, SecretKey secretKey) {
        return new JwtTokenProvider(userDetailsService, secretKey, expiration);
    }
}
