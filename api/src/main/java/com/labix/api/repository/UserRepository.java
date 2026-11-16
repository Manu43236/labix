package com.labix.api.repository;

import com.labix.api.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByTenantIdAndEmail(String tenantId, String email);
    Optional<User> findByTenantIdAndPhone(String tenantId, String phone);
    Page<User> findByTenantId(String tenantId, Pageable pageable);
}
