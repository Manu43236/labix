package com.labix.api.repository;

import com.labix.api.domain.LabTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LabTestRepository extends JpaRepository<LabTest, String> {

    Page<LabTest> findByTenantId(String tenantId, Pageable pageable);

    Page<LabTest> findByTenantIdAndCategory(String tenantId, String category, Pageable pageable);

    Optional<LabTest> findByTenantIdAndCode(String tenantId, String code);

    @Query("SELECT t FROM LabTest t WHERE t.tenantId = :tenantId AND (:q IS NULL OR LOWER(t.name) LIKE %:q% OR LOWER(t.code) LIKE %:q%)")
    Page<LabTest> search(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);
}
