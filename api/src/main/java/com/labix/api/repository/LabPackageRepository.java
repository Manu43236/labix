package com.labix.api.repository;

import com.labix.api.domain.LabPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabPackageRepository extends JpaRepository<LabPackage, String> {

    Page<LabPackage> findByTenantId(String tenantId, Pageable pageable);
}
