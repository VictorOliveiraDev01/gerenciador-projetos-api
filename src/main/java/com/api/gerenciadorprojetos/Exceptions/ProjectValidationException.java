package com.api.gerenciadorprojetos.Exceptions;

import com.api.gerenciadorprojetos.Projects.Entities.Project;
import com.api.gerenciadorprojetos.Users.Entities.User;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * Exceção lançada quando ocorrem erros de validação em operações relacionadas a projetos.
 * Contém informações detalhadas sobre as violações de restrições de validação.
 *
 * @author victor.marcelo
 *
 * @see Project
 * @see ConstraintViolation
 */

public class ProjectValidationException extends ValidationException{
    public ProjectValidationException(String message, Set<ConstraintViolation<Project>> violations) {
        super(message, violations);
    }
}
