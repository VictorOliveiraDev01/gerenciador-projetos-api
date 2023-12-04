package com.api.gerenciadorprojetos.Exceptions;

import com.api.gerenciadorprojetos.Users.Entities.User;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * Exceção lançada quando ocorrem erros de validação em operações relacionadas a usuários.
 * Contém informações detalhadas sobre as violações de restrições de validação.
 *
 * @author victor.marcelo
 *
 * @see User
 * @see ConstraintViolation
 */
public class UserValidationException extends ValidationException {

    public UserValidationException(String message, Set<ConstraintViolation<User>> violations) {
        super(message, violations);
    }
}
