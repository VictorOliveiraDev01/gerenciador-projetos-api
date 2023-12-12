package com.api.gerenciadorprojetos.audit.Services;

import com.api.gerenciadorprojetos.Users.Entities.User;
import com.api.gerenciadorprojetos.audit.Entities.AuditLog;
import com.api.gerenciadorprojetos.audit.Repositories.AuditLogRepository;
import com.api.gerenciadorprojetos.config.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Classe de serviço para gerenciar operações relacionadas ao audit.
 *
 * @author victor.marcelo
 */

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository){
        this.auditLogRepository = auditLogRepository;
    }
    /**
     * Adiciona um Registro de audit para ações de usuário.
     *
     * @param usuario      O usuário que está realizando a ação.
     * @param acao         A ação realizada pelo usuário.
     * @param detalhes     Detalhes sobre a ação.
     * @param entidade     A entidade afetada pela ação.
     * @param requestInfo  Informações sobre a requisição.
     * @throws RuntimeException Se ocorrer um erro ao salvar o registro de auditoria.
     */
    public void addAudit(User usuario, String acao, String detalhes, String entidade, RequestInfo requestInfo) {
        try {
            AuditLog auditLog = criarRegistroAuditoria(usuario, acao, detalhes, entidade, requestInfo);
            log.info("Registrando log de ações do usuário com id: {}", usuario.getId());
            auditLogRepository.save(auditLog);
        } catch (Exception ex) {
            log.error("Erro ao registrar log de ações do usuário de id {}", usuario.getId());
            throw new RuntimeException("Erro ao registrar log de ações do usuário. Causa: " + ex.getMessage(), ex);
        }
    }

    /**
     * Cria o objeto AuditLog com os parametros fornecidos.
     *
     * @param usuario      O usuário que está realizando a ação.
     * @param acao         A ação realizada pelo usuário.
     * @param detalhes     Detalhes sobre a ação.
     * @param entidade     A entidade afetada pela ação.
     * @param requestInfo  Informações sobre o usuário que fez a requisição.
     * @return AuditLog
     */
    public AuditLog criarRegistroAuditoria(User usuario, String acao, String detalhes, String entidade, RequestInfo requestInfo) {
        AuditLog auditLog = new AuditLog();
        auditLog.setHorarioRegistro(LocalDateTime.now());
        auditLog.setUsuario(usuario);
        auditLog.setAcaoRealizada(acao);
        auditLog.setDetalhes(detalhes);
        auditLog.setEntidadeAfetada(entidade);
        auditLog.setEnderecoIP(requestInfo.getIpAddress());
        auditLog.setAgenteUsuario(requestInfo.getUserAgent());
        auditLog.setOrigemAcao(requestInfo.getOrigin());
        auditLog.setInformacoesSessao(requestInfo.getSessionId());
        return auditLog;
    }
}
