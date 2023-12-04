package com.api.gerenciadorprojetos.Utils;

import com.api.gerenciadorprojetos.Exceptions.ProjectValidationException;
import com.api.gerenciadorprojetos.Exceptions.TaskValidationException;
import com.api.gerenciadorprojetos.Exceptions.UnauthorizedException;
import com.api.gerenciadorprojetos.Exceptions.UserValidationException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe que trata exceções globais para a API.
 *
 * @author victor.marcelo
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleGeneralException(Exception e) {
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro em nossos serviços. Causa: " + e.getMessage());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Object> handleHttpClientErrorException(HttpClientErrorException e) {

        HttpStatus status = (e.getStatusCode() == HttpStatus.UNAUTHORIZED) ? HttpStatus.UNAUTHORIZED : (HttpStatus) e.getStatusCode();

        String message = (e.getStatusCode() == HttpStatus.UNAUTHORIZED) ? "Falha ao autenticar." : "Ocorreu um erro em nossos serviços. Causa: ";

        return buildResponseEntity(status, message + e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Requisição inválida. Causa: " + e.getMessage());
    }

    @ExceptionHandler(UserValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleUserValidationException(UserValidationException e) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Erro de validação do usuário. Causa: " + e.getMessage());
    }

    @ExceptionHandler(ProjectValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleProjectValidationException(ProjectValidationException e) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Erro de validação do Projeto. Causa: " + e.getMessage());
    }

    @ExceptionHandler(TaskValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleTaskValidationException(TaskValidationException e) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Erro de validação da Tarefa de projeto. Causa: " + e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Erro de validação. Causa: " + e.getMessage(), errors);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException e) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, "Recurso não encontrado. Causa: " + e.getMessage());
    }
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleEntityNotFoundException(UnauthorizedException e) {
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, "Usuário sem permissão. Causa: " + e.getMessage());
    }

    private ResponseEntity<Object> buildResponseEntity(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new Response<>(status, message));
    }

    private ResponseEntity<Object> buildResponseEntity(HttpStatus status, String message, Map<String, String> errors) {
        return ResponseEntity.status(status).body(new Response<>(status, message, errors));
    }
}
