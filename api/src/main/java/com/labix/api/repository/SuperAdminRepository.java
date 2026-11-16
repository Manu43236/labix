package com.labix.api.repository;

import com.labix.api.domain.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuperAdminRepository extends JpaRepository<SuperAdmin, String> {
    Optional<SuperAdmin> findByEmail(String email);
}
