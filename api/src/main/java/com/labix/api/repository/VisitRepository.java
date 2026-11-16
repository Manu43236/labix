package com.labix.api.repository;

import com.labix.api.domain.Visit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface VisitRepository extends JpaRepository<Visit, String> {

    @Query("SELECT COALESCE(MAX(v.visitNo), 0) FROM Visit v WHERE v.tenantId = :tenantId")
    int findMaxVisitNo(@Param("tenantId") String tenantId);

    Page<Visit> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Visit> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, String status, Pageable pageable);

    Page<Visit> findByTenantIdAndPatientIdAndDeletedAtIsNull(String tenantId, String patientId, Pageable pageable);

    Page<Visit> findByTenantIdAndVisitDateAndDeletedAtIsNull(String tenantId, LocalDate visitDate, Pageable pageable);
}
