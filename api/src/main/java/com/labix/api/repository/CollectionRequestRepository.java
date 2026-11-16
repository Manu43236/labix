package com.labix.api.repository;

import com.labix.api.domain.CollectionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CollectionRequestRepository extends JpaRepository<CollectionRequest, String> {

    Optional<CollectionRequest> findByVisitId(String visitId);

    Page<CollectionRequest> findByTenantId(String tenantId, Pageable pageable);

    Page<CollectionRequest> findByTenantIdAndCollectorId(String tenantId, String collectorId, Pageable pageable);
}
