package com.api.gerenciadorprojetos.audit.Repositories;

import com.api.gerenciadorprojetos.audit.Entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
