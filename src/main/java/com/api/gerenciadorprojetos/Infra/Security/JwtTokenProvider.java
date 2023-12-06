package com.api.gerenciadorprojetos.Infra.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Classe criada para geração, validação e manipulação de tokens JWT.
 *
 * @author victor.marcelo
 */
@Component
public class JwtTokenProvider {

    private final UserDetailsService userDetailsService;
    private final SecretKey secretKey;
    private final long expiration;

    @Autowired
    public JwtTokenProvider(UserDetailsService userDetailsService, SecretKey secretKey,
                            @Value("${jwt.expiration}") long expiration) {
        this.userDetailsService = userDetailsService;
        this.secretKey = secretKey;
        this.expiration = expiration;
    }

    /**
     * Gera um token JWT com base no nome de usuário.
     *
     * @param username O nome de usuário para o qual o token está sendo gerado.
     * @return O token JWT gerado.
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Valida a autenticidade e a validade de um token JWT.
     *
     * @param token O token JWT para validação.
     * @return booleano.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Obtém o nome de usuário associado ao token.
     *
     * @param token O token JWT a ser analisado.
     * @return O nome de usuário extraído do token.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    /**
     * Obtém os detalhes do usuário associados ao token.
     *
     * @param token O token JWT a ser analisado.
     * @return Os detalhes do usuário extraídos do token.
     */
    public UserDetails getUserDetails(String token) {
        String username = getUsernameFromToken(token);
        return userDetailsService.loadUserByUsername(username);
    }
}
