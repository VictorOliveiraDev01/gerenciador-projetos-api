package com.api.gerenciadorprojetos.Exceptions;

import com.api.gerenciadorprojetos.Projects.Entities.Project;
import com.api.gerenciadorprojetos.Users.Entities.User;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

public class ProjectValidationException extends ValidationException{
    public ProjectValidationException(String message, Set<ConstraintViolation<Project>> violations) {
        super(message, violations);
    }
}
