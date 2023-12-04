package com.api.gerenciadorprojetos.Utils;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe que representa a resposta padrão para as operações da API.
 *
 * @author victor.marcelo
 *
 * @param <T> Tipo de dado associado à resposta.
 */
@Data
public class Response<T> {

    private T data;
    private int status;
    private String message;
    private final List<String> errors = new ArrayList<>();
    private final Map<String, String> validationErrors = new HashMap<>();

    public Response(T data) {
        this.data = data;
        this.status = HttpStatus.OK.value();
        this.message = HttpStatus.OK.getReasonPhrase();
    }

    public Response(HttpStatus status, String message) {
        this.status = status.value();
        this.message = message;
    }

    public Response(HttpStatus status, String message, String error) {
        this.status = status.value();
        this.message = message;
        this.errors.add(error);
    }

    public Response(HttpStatus status, String message, List<String> errors) {
        this.status = status.value();
        this.message = message;
        this.errors.addAll(errors);
    }

    public Response(HttpStatus status, String message, Map<String, String> validationErrors) {
        this.status = status.value();
        this.message = message;
        this.validationErrors.putAll(validationErrors);
    }
}
