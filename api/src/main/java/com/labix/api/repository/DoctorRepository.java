package com.labix.api.repository;

import com.labix.api.domain.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorRepository extends JpaRepository<Doctor, String> {

    Page<Doctor> findByTenantId(String tenantId, Pageable pageable);

    @Query("SELECT d FROM Doctor d WHERE d.tenantId = :tenantId AND (:q IS NULL OR LOWER(d.name) LIKE %:q%)")
    Page<Doctor> search(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);
}
