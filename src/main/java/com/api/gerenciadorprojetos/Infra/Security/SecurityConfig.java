package com.api.gerenciadorprojetos.Infra.Security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração de segurança para autenticação baseada no token JWT.
 *
 * @author victor.marcelo
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Construtor da classe SecurityConfig.
     *
     * @param userDetailsService Serviço de detalhes do usuário.
     * @param jwtTokenProvider   Provedor de tokens JWT.
     */
    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, JwtTokenProvider jwtTokenProvider) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Configuração do filtro de autenticação JWT.
     *
     * @return Instância do filtro de autenticação JWT.
     */
    @Bean
    public SecurityFilter jwtAuthenticationFilter() {
        return new SecurityFilter();
    }

    /**
     * Configuração do codificador de senhas.
     *
     * @return Instância do codificador de senhas BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuração da cadeia de filtros de segurança.
     *
     * @param httpSecurity Objeto de configuração do Spring Security.
     * @return A cadeia de filtros de segurança configurada.
     * @throws Exception Exceção em caso de erro na configuração.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return  httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
