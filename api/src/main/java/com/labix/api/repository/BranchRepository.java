package com.labix.api.repository;

import com.labix.api.domain.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, String> {

    List<Branch> findByTenantId(String tenantId);

    boolean existsByTenantIdAndMain(String tenantId, boolean main);
}
