package com.api.gerenciadorprojetos.Utils;

import com.api.gerenciadorprojetos.Users.Entities.User;

/**
 * Representa a resposta de autenticação que inclui informações do usuário e o token JWT.
 *
 * @author victor.marcelo
 */
public class AuthenticationResponse {
    private User user;

    private String userToken;

    public AuthenticationResponse(User user, String userToken){
        this.user = user;
        this.userToken = userToken;
    }
}
