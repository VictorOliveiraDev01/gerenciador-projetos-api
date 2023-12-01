package com.api.gerenciadorprojetos.Exceptions;

import com.api.gerenciadorprojetos.Projects.Entities.Project;
import com.api.gerenciadorprojetos.Tasks.Entities.Task;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

public class TaskValidationException extends ValidationException{
    public TaskValidationException(String message, Set<ConstraintViolation<Task>> violations) {
        super(message, violations);
    }
}
