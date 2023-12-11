package com.api.gerenciadorprojetos.config;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor personalizado para capturar e armazenar informações específicas da requisição.
 *
 * @author victor.marcelo
 */
public class CustomRequestInterceptor implements HandlerInterceptor {

    /**
     * ThreadLocal para armazenar as informações da requisição associadas à thread atual.
     */
    private static final ThreadLocal<RequestInfo> requestInfoThreadLocal = new ThreadLocal<>();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Obtem informações da requisição
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String sessionId = request.getSession().getId();
        String origin = request.getHeader("Origin");

        // Cria um objeto RequestInfo para armazená-lo no ThreadLocal
        RequestInfo requestInfo = new RequestInfo(ipAddress, userAgent, sessionId, origin);
        requestInfoThreadLocal.set(requestInfo);

        // Continua o processamento da requisição
        return true;
    }

    /**
     * Obtém as informações da requisição associadas à thread atual.
     *
     * @return O objeto RequestInfo contendo informações da requisição.
     */
    public static RequestInfo getRequestInfo() {
        // Obtem informações da requisição do ThreadLocal
        return requestInfoThreadLocal.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Limpa o ThreadLocal após a conclusão
        requestInfoThreadLocal.remove();
    }
}
