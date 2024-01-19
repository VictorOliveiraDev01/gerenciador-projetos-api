package com.api.gerenciadorprojetos.Utils;

import com.api.gerenciadorprojetos.Users.Entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author victor.marcelo
 */
public class SecurityUtils {

    /**
     * Obtém o usuário logado
     *
     * @return O usuário logado ou null se não houver usuário autenticado.
     */
    public User getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return (User) authentication.getPrincipal();
    }
}
