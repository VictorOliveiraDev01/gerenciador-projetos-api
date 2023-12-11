package com.api.gerenciadorprojetos.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa informações associadas a uma requisição HTTP.
 *
 * @author victor.marcelo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestInfo {
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String origin;
}
