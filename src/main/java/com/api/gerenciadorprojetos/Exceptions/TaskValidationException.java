package com.api.gerenciadorprojetos.Exceptions;

import com.api.gerenciadorprojetos.Tasks.Entities.Task;
import jakarta.validation.ConstraintViolation;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * Exceção lançada quando ocorrem erros de validação em operações relacionadas a Tarefas de Projeto.
 * Contém informações detalhadas sobre as violações de restrições de validação.
 *
 * @author victor.marcelo
 *
 * @see Task
 * @see ConstraintViolation
 */
public class TaskValidationException extends ValidationException{
    public TaskValidationException(String message, Set<ConstraintViolation<Task>> violations) {
        super(message, violations);
    }

}
