package com.api.gerenciadorprojetos.Exceptions;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * Exceção lançada quando ocorrem erros de validação em dados fornecidos.
 *
 * @author victor.marcelo
 *
 * Contém um conjunto de violações de restrição de validação que fornecem detalhes sobre os problemas encontrados
 * durante a validação.
 */
public class ValidationException extends RuntimeException {

    private final Set<? extends ConstraintViolation<?>> violations;

    public ValidationException(String message, Set<? extends ConstraintViolation<?>> violations) {
        super(message);
        this.violations = violations;
    }

    public Set<? extends ConstraintViolation<?>> getViolations() {
        return violations;
    }
}


