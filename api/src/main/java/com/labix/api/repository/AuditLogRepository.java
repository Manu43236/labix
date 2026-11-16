package com.labix.api.repository;

import com.labix.api.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByAtDesc(String entityType, String entityId);
}
