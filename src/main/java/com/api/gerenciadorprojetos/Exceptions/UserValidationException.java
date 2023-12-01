package com.api.gerenciadorprojetos.Exceptions;

import com.api.gerenciadorprojetos.Users.Entities.User;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

public class UserValidationException extends ValidationException {

    public UserValidationException(String message, Set<ConstraintViolation<User>> violations) {
        super(message, violations);
    }
}
