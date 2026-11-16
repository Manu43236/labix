package com.labix.api.repository;

import com.labix.api.domain.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, String> {

    @Query("SELECT COALESCE(MAX(p.patientNo), 0) FROM Patient p WHERE p.tenantId = :tenantId")
    Long findMaxPatientNo(@Param("tenantId") String tenantId);

    Page<Patient> findByTenantId(String tenantId, Pageable pageable);

    @Query("SELECT p FROM Patient p WHERE p.tenantId = :tenantId AND (:q IS NULL OR LOWER(p.name) LIKE %:q% OR p.phone LIKE %:q%)")
    Page<Patient> search(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);
}
