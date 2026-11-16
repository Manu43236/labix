package com.labix.api.repository;

import com.labix.api.domain.SupportAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportAccessLogRepository extends JpaRepository<SupportAccessLog, String> {
    List<SupportAccessLog> findByTenantIdOrderByStartedAtDesc(String tenantId);
}
