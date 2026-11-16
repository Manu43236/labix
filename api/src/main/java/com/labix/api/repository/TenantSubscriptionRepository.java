package com.labix.api.repository;

import com.labix.api.domain.TenantSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantSubscriptionRepository extends JpaRepository<TenantSubscription, String> {
    Optional<TenantSubscription> findFirstByTenantIdAndStatusOrderByStartDateDesc(String tenantId, String status);
    List<TenantSubscription> findByTenantIdOrderByCreatedAtDesc(String tenantId);
}
